package uk.gov.hmcts.reform.prl.services;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SystemUserServiceTest {
//
//    private static final String USERNAME = "username";
//    private static final String PASSWORD = "password";
//
//    @Mock
//    IdamClient idamClient;
//
//    @Mock
//    OAuth2Configuration auth;
//
//    @Mock
//    SystemUserConfiguration userConfig;
//
//    @InjectMocks
//    SystemUserService systemUserService;
//
//    @Test
//    public void given_ValidUserNameAndPass_shouldReturnToken() {
//
//        when(userConfig.getUserName()).thenReturn(USERNAME);
//        when(userConfig.getPassword()).thenReturn(PASSWORD);
//
//        String expectedToken = idamClient.getAccessToken(USERNAME, PASSWORD);
//
//        when(systemUserService.getSysUserToken()).thenReturn(expectedToken);
//
//    }
//
//    @Test
//    public void shouldReturnSystemUserId() {
//        String token = RandomStringUtils.randomAlphanumeric(10);
//
//        UserInfo userInfo = UserInfo.builder()
//            .uid(UUID.randomUUID().toString())
//            .build();
//
//        when(idamClient.getUserInfo(token)).thenReturn(userInfo);
//
//        String actualId = systemUserService.getUserId(token);
//
//        assertThat(actualId).isEqualTo(userInfo.getUid());
//    }
//
//


}
