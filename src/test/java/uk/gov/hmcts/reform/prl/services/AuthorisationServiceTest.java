package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuthorisationServiceTest {

    @InjectMocks
    AuthorisationService authorisationService;

    @Mock
    ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock
    IdamClient idamClient;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(authorisationService, "s2sAuthorisedServices", "payment_api");
    }

    @Test
    public void authoriseWhenTheServiceIsCalledFromPayment() {

        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertTrue(authorisationService.authoriseService("Bearer abcasda"));

    }

    @Test
    public void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.authoriseService("Bearer abc"));

    }

    @Test
    public void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(authorisationService.authoriseService("Bearer malformed"));
    }

    @Test
    public void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        assertThat(authorisationService.authoriseUser("Bearer abcasda")).isPresent();
    }

    @Test
    public void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertThat(authorisationService.authoriseUser("Bearer malformed")).isEmpty();
    }

    @Test
    public void checkIsAuthorizedForUserAndServiceReturnTrue() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertTrue(authorisationService.isAuthorized("Bearer abcasda", "s2s token"));
    }

    @Test
    public void checkIsAuthorizedForUserAndServiceReturnFalse() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.isAuthorized("Bearer abcasda", "s2s token"));
    }

    @Test
    public void shouldNotAuthoriseServiceWhenCallingServiceIsNull(){
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn(null);

        Boolean authService = authorisationService.authoriseService("bearer abc");

        assertFalse(authService);
    }

    @Test
    public void shouldReturnFalseWhenS2sAuthorisedServicesNotSplit(){
        ReflectionTestUtils.setField(authorisationService, "s2sAuthorisedServices", " " );
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("serviceName");

        Boolean authService = authorisationService.authoriseService("Bearer abc");

        assertFalse(authService);
    }


    @Test
    public void shouldCatchAnExceptionWhenUserTokenIsInvalid() {
        when(idamClient.getUserInfo(any())).thenThrow(new RuntimeException("Not found"));
        Optional<UserInfo> userInfo = authorisationService.authoriseUser("authorisation");

        assertThat(userInfo).isEmpty();

    }

    @Test
    public void shouldReturnUserInfoWhenUserIsAuthorised(){
        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder().build());
        Optional<UserInfo> userInfo = authorisationService.authoriseUser("authorisation");
        assertThat(userInfo).isPresent();
    }
}
