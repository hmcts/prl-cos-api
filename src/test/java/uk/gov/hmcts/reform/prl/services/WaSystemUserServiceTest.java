package uk.gov.hmcts.reform.prl.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.config.WaSystemUserConfiguration;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WaSystemUserServiceTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Mock
    IdamClient idamClient;

    @Mock
    WaSystemUserConfiguration userConfig;

    WaSystemUserService waSystemUserService;

    String token = "";

    @Before
    public void setUp() {
        waSystemUserService = new WaSystemUserService(userConfig, idamClient);
        token = RandomStringUtils.randomAlphanumeric(10);
    }

    @Test
    public void given_ValidUserNameAndPass_shouldReturnToken() {
        when(userConfig.getUserName()).thenReturn(USERNAME);
        when(userConfig.getPassword()).thenReturn(PASSWORD);
        when(idamClient.getAccessToken(anyString(), anyString())).thenReturn(token);

        assertThat(token).isEqualTo(waSystemUserService.getWaSysUserToken());
    }

    @Test
    public void shouldReturnSystemUserId() {
        UserInfo userInfo = UserInfo.builder()
            .uid(UUID.randomUUID().toString())
            .build();

        when(idamClient.getUserInfo(token)).thenReturn(userInfo);

        assertThat(userInfo.getUid()).isEqualTo(waSystemUserService.getUserId(token));
    }
}
