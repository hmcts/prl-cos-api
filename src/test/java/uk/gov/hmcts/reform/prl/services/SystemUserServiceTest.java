package uk.gov.hmcts.reform.prl.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.config.HmcUserConfiguration;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemUserServiceTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String HMC_USERNAME = "hmc-username";
    private static final String HMC_PASSWORD = "hmc-password";

    private IdamClient idamClient;
    private OAuth2Configuration auth;
    private SystemUserConfiguration userConfig;
    private HmcUserConfiguration hmcUserConfig;

    private SystemUserService systemUserService;
    private String token;

    @BeforeEach
    void setUp() {
        idamClient = mock(IdamClient.class);
        auth = mock(OAuth2Configuration.class);
        userConfig = mock(SystemUserConfiguration.class);
        hmcUserConfig = mock(HmcUserConfiguration.class);

        systemUserService = new SystemUserService(auth, userConfig, idamClient, hmcUserConfig);
        token = RandomStringUtils.randomAlphanumeric(10);
    }

    @Test
    void givenValidUserNameAndPassShouldReturnToken() {
        when(userConfig.getUserName()).thenReturn(USERNAME);
        when(userConfig.getPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(anyString(), anyString())).thenReturn(token);

        assertThat(systemUserService.getSysUserToken()).isEqualTo(token);
    }

    @Test
    void shouldReturnSystemUserId() {
        UserInfo userInfo = UserInfo.builder()
            .uid(UUID.randomUUID().toString())
            .build();

        when(idamClient.getUserInfo(token)).thenReturn(userInfo);

        assertThat(systemUserService.getUserId(token)).isEqualTo(userInfo.getUid());
    }

    @Test
    void shouldReturnHmcTokenWhenCredentialsAreValid() {
        when(hmcUserConfig.getHmcCftUserName()).thenReturn(HMC_USERNAME);
        when(hmcUserConfig.getHmcCftUserPassword()).thenReturn(HMC_PASSWORD);
        when(idamClient.getAccessToken(HMC_USERNAME, HMC_PASSWORD)).thenReturn(token);

        assertThat(systemUserService.getHmcUserToken()).isEqualTo(token);
        verify(idamClient).getAccessToken(HMC_USERNAME, HMC_PASSWORD);
    }

    @Test
    void shouldThrowWhenHmcCredentialsMissing() {
        when(hmcUserConfig.getHmcCftUserName()).thenReturn(" ");
        when(hmcUserConfig.getHmcCftUserPassword()).thenReturn(HMC_PASSWORD);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> systemUserService.getHmcUserToken());
        assertThat(ex.getMessage()).isEqualTo("Missing IDAM credentials for hearing CFT user");
    }

    @Test
    void shouldRetryAndSucceedOnThirdAttempt() throws Exception {
        Method method = SystemUserService.class.getDeclaredMethod("getAccessTokenWithRetry", String.class, String.class);
        method.setAccessible(true);

        RuntimeException first = new RuntimeException("first");
        RuntimeException second = new RuntimeException("second");

        when(idamClient.getAccessToken(HMC_USERNAME, HMC_PASSWORD))
            .thenThrow(first)
            .thenThrow(second)
            .thenReturn(token);

        String result = (String) method.invoke(systemUserService, HMC_USERNAME, HMC_PASSWORD);

        assertThat(result).isEqualTo(token);
        verify(idamClient, times(3)).getAccessToken(HMC_USERNAME, HMC_PASSWORD);
    }

    @Test
    void shouldThrowAfterMaxRetries() throws Exception {
        Method method = SystemUserService.class.getDeclaredMethod("getAccessTokenWithRetry", String.class, String.class);
        method.setAccessible(true);

        RuntimeException boom = new RuntimeException("boom");
        when(idamClient.getAccessToken(HMC_USERNAME, HMC_PASSWORD)).thenThrow(boom);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
            () -> method.invoke(systemUserService, HMC_USERNAME, HMC_PASSWORD));

        Throwable cause = ex.getCause();
        assertThat(cause).isInstanceOf(IllegalStateException.class);
        assertThat(cause.getMessage()).isEqualTo("Failed to generate HMC IDAM token after retries");
        verify(idamClient, times(3)).getAccessToken(HMC_USERNAME, HMC_PASSWORD);
    }

    @Test
    void validateCredentialsShouldThrowForBlankValues() throws Exception {
        Method method = SystemUserService.class.getDeclaredMethod("validateCredentials", String.class, String.class);
        method.setAccessible(true);

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
            () -> method.invoke(systemUserService, " ", HMC_PASSWORD));

        assertThat(ex.getCause()).isInstanceOf(IllegalStateException.class);
        assertThat(ex.getCause().getMessage()).isEqualTo("Missing IDAM credentials for hearing CFT user");
    }

    @Test
    void sleepBeforeRetryShouldSetInterruptFlagAndThrowWhenInterrupted() throws Exception {
        Method method = SystemUserService.class.getDeclaredMethod("sleepBeforeRetry");
        method.setAccessible(true);

        Thread.currentThread().interrupt();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> method.invoke(systemUserService));

        assertThat(ex.getCause()).isInstanceOf(IllegalStateException.class);
        assertThat(ex.getCause().getMessage()).isEqualTo("Retry interrupted while generating HMC IDAM token");
        assertThat(Thread.currentThread().isInterrupted()).isTrue();

        Thread.interrupted();
    }
}
