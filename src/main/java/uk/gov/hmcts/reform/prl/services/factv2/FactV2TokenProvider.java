package uk.gov.hmcts.reform.prl.services.factv2;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactV2TokenProvider {
    private static final long TOKEN_EXPIRY_SECONDS = 60;

    private final RestTemplate restTemplate;

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

    private volatile String accessToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public String getAccessToken() {
        if (tokenIsUsable()) {
            return accessToken;
        }
        synchronized (this) {
            if (tokenIsUsable()) {
                return accessToken;
            }
            validateConfiguration();
            accessToken = requestToken();
            return accessToken;
        }
    }

    private String requestToken() {
        log.info("Attempting to acquire FaCT V2 access token from {}", resolveTokenUrl());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        TokenResponse response = restTemplate.postForObject(resolveTokenUrl(), new HttpEntity<>(form, headers), TokenResponse.class);
        if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
            throw new IllegalStateException("FaCT V2 token endpoint returned no access token");
        }
        long expiresIn = response.getExpiresIn() == null ? 300 : response.getExpiresIn();
        tokenExpiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn - TOKEN_EXPIRY_SECONDS));
        log.info("Acquired FaCT V2 access token, expires at {}", tokenExpiresAt);
        return response.getAccessToken();
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

    private boolean tokenIsUsable() {
        return accessToken != null && Instant.now().isBefore(tokenExpiresAt);
    }

    @Data
    public static class TokenResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        private String accessToken;
        @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
        private Long expiresIn;
    }
}
