package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.prl.exception.AuthorisationException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationServiceTest {

    @InjectMocks
    AuthorisationService authorisationService;

    @Mock
    ServiceAuthorisationApi serviceAuthorisationApi;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(authorisationService, "s2sAuthorisedServices", "payment_api");
    }

    @Test
    public void authoriseWhenTheServiceIsCalledFromPayment() {

        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_api");
        assertTrue(authorisationService.authorise("Bearer abcasda"));

    }

    @Test(expected = AuthorisationException.class)
    public void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        authorisationService.authorise("Bearer abcasda");

    }

    @Test(expected = AuthorisationException.class)
    public void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        authorisationService.authorise("Bearer malformed");
    }


}
