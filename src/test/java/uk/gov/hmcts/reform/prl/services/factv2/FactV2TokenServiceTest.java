package uk.gov.hmcts.reform.prl.services.factv2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FactV2TokenServiceTest {
    private static final String TOKEN_URL = "https://login.microsoftonline.com/tenant/oauth2/v2.0/token";

    private RestTemplate restTemplate;
    private ConcurrentMapCacheManager cacheManager;
    private FactV2TokenService tokenService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        cacheManager = new ConcurrentMapCacheManager(FactV2TokenService.FACT_V2_TOKEN_CACHE);
        tokenService = new FactV2TokenService(restTemplate, cacheManager);
        ReflectionTestUtils.setField(tokenService, "tokenUrl", TOKEN_URL);
        ReflectionTestUtils.setField(tokenService, "clientId", "client-id");
        ReflectionTestUtils.setField(tokenService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(tokenService, "scope", "api://fact-app/.default");
    }

    @Test
    void cachesTokenUntilItsExpiryWindow() {
        FactV2TokenService.TokenResponse response = new FactV2TokenService.TokenResponse();
        response.setAccessToken("access-token");
        response.setExpiresIn(3600L);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(FactV2TokenService.TokenResponse.class)))
            .thenReturn(response);

        assertEquals("access-token", tokenService.getAccessToken());
        assertEquals("access-token", tokenService.getAccessToken());

        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(FactV2TokenService.TokenResponse.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clientId", "clientSecret", "scope"})
    void rejectsIncompleteOAuthConfiguration(String missingField) {
        ReflectionTestUtils.setField(tokenService, missingField, "");

        IllegalStateException failure = assertThrows(IllegalStateException.class, tokenService::getAccessToken);

        assertEquals("FaCT V2 OAuth configuration is incomplete", failure.getMessage());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void rejectsMissingTokenUrlAndTenant() {
        ReflectionTestUtils.setField(tokenService, "tokenUrl", "");
        ReflectionTestUtils.setField(tokenService, "tenantId", "");

        assertThrows(IllegalStateException.class, tokenService::getAccessToken);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void buildsTokenUrlFromTenantWhenExplicitUrlIsNotConfigured() {
        String tenantTokenUrl = "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token";
        ReflectionTestUtils.setField(tokenService, "tokenUrl", "");
        ReflectionTestUtils.setField(tokenService, "tenantId", "test-tenant");
        FactV2TokenService.TokenResponse response = tokenResponse("access-token", 3600L);
        when(restTemplate.postForObject(eq(tenantTokenUrl), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(response);

        assertEquals("access-token", tokenService.getAccessToken());

        verify(restTemplate).postForObject(eq(tenantTokenUrl), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class));
    }

    @Test
    void rejectsNullTokenResponse() {
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(null);

        assertThrows(IllegalStateException.class, tokenService::getAccessToken);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingOrBlankAccessToken(String accessToken) {
        FactV2TokenService.TokenResponse response = tokenResponse(accessToken, 3600L);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(response);

        assertThrows(IllegalStateException.class, tokenService::getAccessToken);
    }

    @Test
    void usesDefaultExpiryWhenResponseOmitsExpiresIn() {
        FactV2TokenService.TokenResponse response = tokenResponse("access-token", null);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(response);

        assertEquals("access-token", tokenService.getAccessToken());
        assertEquals("access-token", tokenService.getAccessToken());

        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class));
    }

    @ParameterizedTest
    @MethodSource("tokenLifetimes")
    void refreshesTokenAtBufferBoundaryAndReusesReplacement(Long expiresIn, long refreshAfterSeconds) {
        Instant issuedAt = Instant.parse("2026-11-02T10:00:00Z");
        Instant beforeRefresh = issuedAt.plusSeconds(refreshAfterSeconds - 1);
        Instant refreshAt = issuedAt.plusSeconds(refreshAfterSeconds);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class)))
            .thenReturn(tokenResponse("first-token", expiresIn), tokenResponse("replacement-token", 3600L));

        try (MockedStatic<Instant> clock = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            clock.when(Instant::now).thenReturn(issuedAt);
            assertEquals("first-token", tokenService.getAccessToken());

            clock.when(Instant::now).thenReturn(beforeRefresh);
            assertEquals("first-token", tokenService.getAccessToken());
            verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
                eq(FactV2TokenService.TokenResponse.class));

            clock.when(Instant::now).thenReturn(refreshAt);
            assertEquals("replacement-token", tokenService.getAccessToken());
            assertEquals("replacement-token", tokenService.getAccessToken());
            verify(restTemplate, times(2)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
                eq(FactV2TokenService.TokenResponse.class));
        }
    }

    @Test
    void sendsClientCredentialsAsAFormEncodedRequest() {
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(tokenResponse("access-token", 3600L));

        assertEquals("access-token", tokenService.getAccessToken());

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> request = ArgumentCaptor.captor();
        verify(restTemplate).postForObject(eq(TOKEN_URL), request.capture(), eq(FactV2TokenService.TokenResponse.class));
        assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getValue().getHeaders().getContentType());
        assertEquals(Map.of(
            "client_id", List.of("client-id"),
            "client_secret", List.of("client-secret"),
            "grant_type", List.of("client_credentials"),
            "scope", List.of("api://fact-app/.default")
        ), request.getValue().getBody());
    }

    @Test
    void retriesAfterTokenEndpointFailureAndCachesSuccessfulResponse() {
        RestClientException failure = new RestClientException("Token endpoint unavailable");
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class)))
            .thenThrow(failure).thenReturn(tokenResponse("access-token", 3600L));

        assertSame(failure, assertThrows(RestClientException.class, tokenService::getAccessToken));
        assertEquals("access-token", tokenService.getAccessToken());
        assertEquals("access-token", tokenService.getAccessToken());

        verify(restTemplate, times(2)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class));
    }

    @Test
    void failsClearlyWhenTokenCacheIsNotConfigured() {
        FactV2TokenService unconfiguredService = new FactV2TokenService(
            restTemplate, new ConcurrentMapCacheManager("unrelated-cache"));

        NullPointerException failure = assertThrows(NullPointerException.class, unconfiguredService::getAccessToken);

        assertEquals("FaCT V2 token cache is not configured", failure.getMessage());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void concurrentRequestsShareOneTokenAcquisition() throws Exception {
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class))).thenReturn(tokenResponse("access-token", 3600L));
        int callers = 6;
        CountDownLatch ready = new CountDownLatch(callers);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(callers)) {
            List<Future<String>> results = IntStream.range(0, callers).mapToObj(index -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting to start token requests");
                }
                return tokenService.getAccessToken();
            })).toList();
            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS), "All callers should be ready before they start");
                start.countDown();
                for (Future<String> result : results) {
                    assertEquals("access-token", result.get(5, TimeUnit.SECONDS));
                }
            } finally {
                start.countDown();
            }
        }

        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenService.TokenResponse.class));
    }

    private static Stream<Arguments> tokenLifetimes() {
        return Stream.of(
            Arguments.of(3600L, 3540L),
            Arguments.of(null, 240L),
            Arguments.of(30L, 1L)
        );
    }

    private FactV2TokenService.TokenResponse tokenResponse(String accessToken, Long expiresIn) {
        FactV2TokenService.TokenResponse response = new FactV2TokenService.TokenResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(expiresIn);
        return response;
    }
}
