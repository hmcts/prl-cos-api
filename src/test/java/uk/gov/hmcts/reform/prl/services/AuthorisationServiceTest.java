package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    @InjectMocks
    AuthorisationService authorisationService;

    @Mock
    ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock
    IdamClient idamClient;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(authorisationService, "s2sAuthorisedServices", "payment_api");
    }

    @Test
    void authoriseWhenTheServiceIsCalledFromPayment() {

        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");

        assertTrue(authorisationService.authoriseService("Bearer token"));
    }

    @Test
    void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");

        assertFalse(authorisationService.authoriseService("Bearer abc"));
    }

    @Test
    void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(authorisationService.authoriseService("Bearer malformed"));
    }

    @Test
    void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());

        assertTrue(authorisationService.authoriseUser("Bearer token"));
    }

    @Test
    void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertFalse(authorisationService.authoriseUser("Bearer malformed"));
    }

    @Test
    void checkIsAuthorizedForUserAndServiceReturnTrue() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");

        assertTrue(authorisationService.isAuthorized("Bearer token", "s2s token"));
    }

    @Test
    void checkIsAuthorizedForUserAndServiceReturnFalse() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");

        assertFalse(authorisationService.isAuthorized("Bearer token", "s2s token"));
    }
}
