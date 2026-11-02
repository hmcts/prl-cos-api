package uk.gov.hmcts.reform.prl.services.factv2;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactV2TokenService {
    public static final String FACT_V2_TOKEN_CACHE = "factV2TokenCache";
    private static final String FACT_V2_ACCESS_TOKEN_CACHE_KEY = "accessToken";
    private static final long TOKEN_REFRESH_BUFFER_SECONDS = 60;

    private final RestTemplate restTemplate;
    private final CacheManager cacheManager;

    @Value("${fact.v2.oauth.token-url:}")
    private String tokenUrl;

    @Value("${fact.v2.oauth.tenant-id:}")
    private String tenantId;

    @Value("${fact.v2.oauth.client-id:}")
    private String clientId;

    @Value("${fact.v2.oauth.client-secret:}")
    private String clientSecret;

    @Value("${fact.v2.oauth.scope:}")
    private String scope;

    public synchronized String getAccessToken() {
        Cache tokenCache = Objects.requireNonNull(cacheManager.getCache(FACT_V2_TOKEN_CACHE),
                                                  "FaCT V2 token cache is not configured");
        CachedToken cachedToken = getCachedToken(tokenCache);
        if (cachedToken != null && cachedToken.isUsable()) {
            log.info("Using cached FaCT V2 access token; refreshAt={}", cachedToken.getRefreshAt());
            return cachedToken.getAccessToken();
        }

        if (cachedToken != null) {
            tokenCache.evict(FACT_V2_ACCESS_TOKEN_CACHE_KEY);
        }

        validateConfiguration();
        CachedToken newToken = requestToken();
        tokenCache.put(FACT_V2_ACCESS_TOKEN_CACHE_KEY, newToken);

        return newToken.getAccessToken();
    }

    private CachedToken getCachedToken(Cache tokenCache) {
        Cache.ValueWrapper cachedValue = tokenCache.get(FACT_V2_ACCESS_TOKEN_CACHE_KEY);
        return cachedValue == null ? null : (CachedToken) cachedValue.get();
    }

    private CachedToken requestToken() {
        log.info("Attempting to acquire FaCT V2 access token from {}", resolveTokenUrl());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        TokenResponse response = restTemplate.postForObject(
            resolveTokenUrl(), new HttpEntity<>(form, headers), TokenResponse.class);
        if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
            throw new IllegalStateException("FaCT V2 token endpoint returned no access token");
        }
        long expiresIn = response.getExpiresIn() == null ? 300 : response.getExpiresIn();
        Instant refreshAt = Instant.now().plusSeconds(Math.max(1, expiresIn - TOKEN_REFRESH_BUFFER_SECONDS));
        log.info("Acquired FaCT V2 access token; cached token will be refreshed at {}", refreshAt);
        return new CachedToken(response.getAccessToken(), refreshAt);
    }

    private void validateConfiguration() {
        if ((tokenUrl.isBlank() && tenantId.isBlank()) || clientId.isBlank() || clientSecret.isBlank() || scope.isBlank()) {
            throw new IllegalStateException("FaCT V2 OAuth configuration is incomplete");
        }
    }

    private String resolveTokenUrl() {
        return tokenUrl.isBlank()
            ? "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token"
            : tokenUrl;
    }

    @Data
    private static class CachedToken {
        private final String accessToken;
        private final Instant refreshAt;

        private boolean isUsable() {
            return accessToken != null && Instant.now().isBefore(refreshAt);
        }
    }

    @Data
    public static class TokenResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        private String accessToken;
        @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
        private Long expiresIn;
    }
}
