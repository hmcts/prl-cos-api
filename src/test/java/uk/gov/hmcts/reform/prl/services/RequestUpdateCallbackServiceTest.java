package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RequestUpdateCallbackServiceTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "C100";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "paymentSuccessCallback";
    private final String userToken = "Bearer testToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    private ServiceRequestUpdateDto serviceRequestUpdateDto;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private ConfidentialityTabService confidentialityTabService;


    @InjectMocks
    RequestUpdateCallbackService requestUpdateCallbackService;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
    }

    @Test(expected = NullPointerException.class)
    public void shouldStartAndSubmitEventWithCaseDetails() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf("123")).build();
        when(coreCaseDataApi.getCase(userToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(buildStartEventResponse(eventName, eventToken));
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .build();

        CaseData caseData = CaseData.builder().build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataApi).startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), eventName
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), true,
                                                         buildCaseDataContent(eventName, eventToken, null)
        );
    }

    @Test
    public void shouldNotStartOrSubmitEventWithoutCaseDetails() throws Exception {

        CaseDetails caseDetails = CaseDetails.builder()
            .build();
        when(coreCaseDataApi.getCase(userToken, serviceAuthToken, "123")).thenReturn(caseDetails);

        assertThrows(Exception.class, () -> {
            serviceRequestUpdateDto = ServiceRequestUpdateDto.builder().ccdCaseNumber("123").serviceRequestStatus(
                "Paid").build();

            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        });

    }

    @Test
    public void shouldProcessCallback() throws Exception {

        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf("123")).build();
        when(coreCaseDataApi.getCase(userToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(confidentialityTabService.updateConfidentialityDetails(any(), any())).thenReturn(true);
        when(coreCaseDataApi.startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(buildStartEventResponse(eventName, eventToken));
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paid")
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        assertEquals(coreCaseDataApi.getCase(userToken, serviceAuthToken, caseId.toString()), caseDetails);

    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, Object caseData) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id(eventId)
                       .build())
            .data(caseData)
            .build();
    }

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }
}
