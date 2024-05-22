package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RequestUpdateCallbackServiceTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "paymentSuccessCallback";
    private final String eventFailureName = "paymentFailureCallback";
    public static final String authToken = "Bearer TestAuthToken";
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

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private Court court;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @InjectMocks
    RequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    private StartEventResponse startEventResponse;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(coreCaseDataService.eventRequest(CaseEvent.PAYMENT_SUCCESS_CALLBACK, systemUserId)).thenReturn(
            EventRequestData.builder().build());
        caseDetails = CaseDetails.builder().id(Long.valueOf("123")).data(Map.of("id", 1)).build();
        startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.findCaseById(
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(startEventResponse);
        when(partyLevelCaseFlagsService.generateC100AllPartyCaseFlags(any(), any())).thenCallRealMethod();
    }

    @Test(expected = NullPointerException.class)
    public void shouldStartAndSubmitEventWithCaseDetails() throws Exception {
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(startEventResponse);
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

        verify(coreCaseDataApi).startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), eventName
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), true,
                                                         buildCaseDataContent(eventName, eventToken, null)
        );
    }

    @Test
    public void shouldNotStartOrSubmitEventWithoutCaseDetails() throws Exception {

        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, "123")).thenReturn(caseDetails);

        assertThrows(Exception.class, () -> {
            serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
                .ccdCaseNumber("123")
                .serviceRequestStatus(
                    "Paid").build();

            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        });

    }

    @Test
    public void shouldProcessCallback() throws Exception {
        CaseData caseData = CaseData.builder().id(1L)
            .paymentServiceRequestReferenceNumber("test-reference").build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(allTabService.updateAllTabsIncludingConfTab(Mockito.anyString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(startEventResponse);
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
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
        assertEquals(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString()), caseDetails);

    }

    @Test
    public void shouldProcessCallbackNotPaid() throws Exception {
        CaseData caseData = CaseData.builder().id(1L)
            .paymentServiceRequestReferenceNumber("test-reference").build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(allTabService.updateAllTabsIncludingConfTab(Mockito.anyString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(startEventResponse);
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("test")
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        assertEquals(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString()), caseDetails);

    }

    @Test
    public void shouldProcessPendingCallback() throws Exception {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle
                                                     .builder().payment(Payment.builder()
                                                                            .paymentServiceRequestReferenceNumber("Paid")
                                                                            .build())
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(ApplicationStatus
                                                                                                         .SUBMITTED
                                                                                                         .getDisplayedValue()).build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(ApplicationStatus
                                                                                                  .SUBMITTED.getDisplayedValue()).build())
                                                     .build()));
        UploadAdditionalApplicationData uploadAdditionalApplicationData = UploadAdditionalApplicationData.builder()
            .additionalApplicationsApplyingFor(List.of(AdditionalApplicationTypeEnum.otherOrder))
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().build())
            .additionalApplicationFeesToPay("£232.00")
            .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder().urgencyTimeFrameType(
                UrgencyTimeFrameType.WITHIN_2_DAYS).build())
            .build();
        CaseData caseData = CaseData.builder().id(1L).additionalApplicationsBundle(additionalApplicationsBundle)
            .paymentServiceRequestReferenceNumber("test-reference").uploadAdditionalApplicationData(uploadAdditionalApplicationData).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(allTabService.updateAllTabsIncludingConfTab(Mockito.anyString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventFailureName
        ))
            .thenReturn(startEventResponse);
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(court);
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
            .serviceRequestReference("Paid")
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        assertEquals(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString()), caseDetails);

    }

    @Test
    public void shouldThrowExceptionForProcessCallback() throws Exception {
        CaseData caseData = CaseData.builder().id(1L)
            .paymentServiceRequestReferenceNumber("test-reference").build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(allTabService.updateAllTabsIncludingConfTab(Mockito.anyString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName
        ))
            .thenReturn(startEventResponse);
        when(courtFinderService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenThrow(new RuntimeException());
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
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
        assertEquals(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString()), caseDetails);

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
}
