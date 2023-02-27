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
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

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

    private final String userToken = "Bearer testToken";

    private String bearerToken;

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
    private CourtFinderService courtFinderService;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @InjectMocks
    RequestUpdateCallbackService requestUpdateCallbackService;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
    }

    @Test(expected = NullPointerException.class)
    public void shouldStartAndSubmitEventWithCaseDetails() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf("123")).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(coreCaseDataApi.startEventForCaseWorker(authToken, serviceAuthToken, systemUserId, jurisdiction,
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

        CaseDetails caseDetails = CaseDetails.builder()
            .build();
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
        CaseData caseData = CaseData.builder().id(1L).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(1L).data(stringObjectMap)
                .createdDate(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paid")
            .build();
        CaseEvent caseEvent = CaseEvent.PAYMENT_SUCCESS_CALLBACK;
        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUserId)
            .userToken(userToken)
            .build();
        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.UPDATE_ALL_TABS.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUserId)
            .userToken(userToken)
            .build();
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUserId);
        when(coreCaseDataService.eventRequest(caseEvent, systemUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUserId)).thenReturn(allTabsUpdateEventRequestData);


        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();
        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);

        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                String.valueOf(caseData.getId()),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        assertEquals(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString()), caseDetails);

    }

    @Test
    public void shouldProcessPendingCallback() throws Exception {
        CaseData caseData = CaseData.builder().id(1L).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(1L).data(stringObjectMap).build();
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        doNothing().when(allTabService).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseDetails.getId().toString())
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paidn")
            .build();

        CaseEvent caseEvent = CaseEvent.PAYMENT_FAILURE_CALLBACK;
        EventRequestData eventRequestData = EventRequestData.builder()
            .eventId(caseEvent.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUserId)
            .userToken(userToken)
            .build();
        EventRequestData allTabsUpdateEventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.UPDATE_ALL_TABS.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUserId)
            .userToken(userToken)
            .build();
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUserId);
        when(coreCaseDataService.eventRequest(caseEvent, systemUserId)).thenReturn(eventRequestData);
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUserId)).thenReturn(allTabsUpdateEventRequestData);


        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();
        when(coreCaseDataService.createCaseDataContent(startEventResponse,caseDataUpdated)).thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);

        StartEventResponse allTabsUpdateStartEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(userToken).build();
        CaseData caseDataUpdatedforAllTabs = CaseUtils.getCaseDataFromStartUpdateEventResponse(allTabsUpdateStartEventResponse, objectMapper);
        when(coreCaseDataService.startUpdate(
            userToken,allTabsUpdateEventRequestData, String.valueOf(caseData.getId()),true))
            .thenReturn(allTabsUpdateStartEventResponse);

        doNothing().when(allTabService).updateAllTabsIncludingConfTabRefactored(userToken,
                                                                                String.valueOf(caseData.getId()),
                                                                                allTabsUpdateStartEventResponse,
                                                                                allTabsUpdateEventRequestData,
                                                                                caseData);


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

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }

}
