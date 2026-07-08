package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.class)
public class FeeAndPayServiceRequestControllerTest {

    private static final String authToken = "Bearer TestAuthToken";
    private static final String s2sToken = "s2s AuthToken";

    @InjectMocks
    private FeeAndPayServiceRequestController feeAndPayServiceRequestController;

    @Mock
    private AuthorisationService authorisationService;


    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private EventService eventPublisher;

    List<String> errorList;



    @Before
    public void setUp() {
        errorList = new ArrayList<>();
    }


    @Test
    public void testCcdSubmittedHelpWithFeesYes() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.Yes)
                                                            .build()).build()).build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> response = feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testCcdSubmittedHelpWithhFeesNo() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().caseId("123")
                             .state("PENDING").caseData(CaseData.builder()
                                                            .applicantSolicitorEmailAddress("hello@gmail.com")
                                                            .helpWithFees(YesOrNo.No)
                                                            .build()).build()).build();
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> response = feeAndPayServiceRequestController.ccdSubmitted(authToken, s2sToken, callbackRequest);
        Assert.assertNotNull(response);
    }



    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
