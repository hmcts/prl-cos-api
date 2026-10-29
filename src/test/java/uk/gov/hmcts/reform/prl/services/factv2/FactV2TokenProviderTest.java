package uk.gov.hmcts.reform.prl.services.factv2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FactV2TokenProviderTest {
    private static final String TOKEN_URL = "https://login.microsoftonline.com/tenant/oauth2/v2.0/token";

    private RestTemplate restTemplate;
    private FactV2TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        tokenProvider = new FactV2TokenProvider(restTemplate);
        ReflectionTestUtils.setField(tokenProvider, "tokenUrl", TOKEN_URL);
        ReflectionTestUtils.setField(tokenProvider, "clientId", "client-id");
        ReflectionTestUtils.setField(tokenProvider, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(tokenProvider, "scope", "api://fact-app/.default");
    }

    @Test
    void cachesTokenUntilItsExpiryWindow() {
        FactV2TokenProvider.TokenResponse response = new FactV2TokenProvider.TokenResponse();
        response.setAccessToken("access-token");
        response.setExpiresIn(3600L);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(FactV2TokenProvider.TokenResponse.class)))
            .thenReturn(response);

        assertEquals("access-token", tokenProvider.getAccessToken());
        assertEquals("access-token", tokenProvider.getAccessToken());

        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(FactV2TokenProvider.TokenResponse.class));
    }

    @Test
    void rejectsIncompleteOAuthConfiguration() {
        ReflectionTestUtils.setField(tokenProvider, "clientSecret", "");

        assertThrows(IllegalStateException.class, tokenProvider::getAccessToken);
    }

    @Test
    void buildsTokenUrlFromTenantWhenExplicitUrlIsNotConfigured() {
        String tenantTokenUrl = "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token";
        ReflectionTestUtils.setField(tokenProvider, "tokenUrl", "");
        ReflectionTestUtils.setField(tokenProvider, "tenantId", "test-tenant");
        FactV2TokenProvider.TokenResponse response = tokenResponse("access-token", 3600L);
        when(restTemplate.postForObject(eq(tenantTokenUrl), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class))).thenReturn(response);

        assertEquals("access-token", tokenProvider.getAccessToken());

        verify(restTemplate).postForObject(eq(tenantTokenUrl), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class));
    }

    @Test
    void rejectsNullTokenResponse() {
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class))).thenReturn(null);

        assertThrows(IllegalStateException.class, tokenProvider::getAccessToken);
    }

    @Test
    void rejectsBlankAccessToken() {
        FactV2TokenProvider.TokenResponse response = tokenResponse(" ", 3600L);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class))).thenReturn(response);

        assertThrows(IllegalStateException.class, tokenProvider::getAccessToken);
    }

    @Test
    void usesDefaultExpiryWhenResponseOmitsExpiresIn() {
        FactV2TokenProvider.TokenResponse response = tokenResponse("access-token", null);
        when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class))).thenReturn(response);

        assertEquals("access-token", tokenProvider.getAccessToken());
        assertEquals("access-token", tokenProvider.getAccessToken());

        verify(restTemplate, times(1)).postForObject(eq(TOKEN_URL), any(HttpEntity.class),
            eq(FactV2TokenProvider.TokenResponse.class));
    }

    private FactV2TokenProvider.TokenResponse tokenResponse(String accessToken, Long expiresIn) {
        FactV2TokenProvider.TokenResponse response = new FactV2TokenProvider.TokenResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(expiresIn);
        return response;
    }
}
