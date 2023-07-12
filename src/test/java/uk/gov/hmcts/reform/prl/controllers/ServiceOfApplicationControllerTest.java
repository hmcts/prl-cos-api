package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationControllerTest {

    @InjectMocks
    private ServiceOfApplicationController serviceOfApplicationController;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private WelshCourtEmail welshCourtEmail;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "internal-update-all-tabs";

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testServiceOfApplicationAboutToStart() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("first")
            .lastName("last")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first")
            .representativeLastName("last")
            .build();
        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(partyDetails)))
            .respondents(List.of(element(partyDetails)))
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder()
                                                    .otherDetails(OtherOrderDetails.builder().orderCreatedDate("").build())
                                                    .orderType("Test").build())
                                         .build()))
            .c8Document(finalDoc)
            .c8FormDocumentsUploaded(documentList)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        stringObjectMap.put("serviceOfApplicationHeader","TestHeader");
        stringObjectMap.put("option1","1");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        stringObjectMap.put(SOA_CONFIDENTIAL_DETAILS_PRESENT, YesOrNo.Yes);
        stringObjectMap.put(CASE_CREATED_BY, CaseCreatedBy.SOLICITOR);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();
        String courtEmail = "test1@test.com";
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(any())).thenReturn(courtEmail);

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(stringObjectMap);
        when(serviceOfApplicationService.getSoaCaseFieldsMap(Mockito.any())).thenReturn(stringObjectMap);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = serviceOfApplicationController
            .handleAboutToStart(authToken, s2sToken, callbackRequest);
        assertEquals(CaseCreatedBy.SOLICITOR, aboutToStartOrSubmitCallbackResponse.getData().get(CASE_CREATED_BY));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(Long.parseLong(TEST_CASE_ID)).applicantCaseName("xyz")
            .caseTypeOfApplication("FL401")
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder()
                                                    .otherDetails(OtherOrderDetails.builder().orderCreatedDate("").build())
                                                    .orderType("Test").build())
                                         .build()))
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);

        when(serviceOfApplicationService.getCollapsableOfSentDocuments()).thenReturn("Collapsable");
        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(stringObjectMap);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();


        when(objectMapper.convertValue(stringObjectMap,  CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        when(launchDarklyClient.isFeatureEnabled("soa-access-code-gov-notify")).thenReturn(false);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(jurisdiction, caseType, caseId, eventName, eventData);

        final ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponseResponseEntity = serviceOfApplicationController.handleSubmitted(
            authToken,
            s2sToken,
            callbackRequest);

        assertEquals(HttpStatus.OK, submittedCallbackResponseResponseEntity.getStatusCode());
    }

    @Test
    public void testHandleAboutToSubmitWhenProceedToServingNo() throws Exception {
        CaseData caseData = CaseData.builder().id(Long.parseLong(TEST_CASE_ID))
            .serviceOfApplication(ServiceOfApplication.builder().proceedToServing(YesOrNo.No).build())
                .applicantCaseName("xyz").build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();


        when(objectMapper.convertValue(stringObjectMap,  CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(launchDarklyClient.isFeatureEnabled("soa-access-code-gov-notify")).thenReturn(false);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(jurisdiction, caseType, caseId, eventName, eventData);

        final ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponseResponseEntity = serviceOfApplicationController
            .handleSubmitted(authToken, s2sToken, callbackRequest);

        assertEquals(HttpStatus.OK, submittedCallbackResponseResponseEntity.getStatusCode());
    }

    @Test
    public void testExceptionForServiceOfApplicationAboutToStart() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("first")
            .lastName("last")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first")
            .representativeLastName("last")
            .build();
        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(partyDetails)))
            .respondents(List.of(element(partyDetails)))
            .orderCollection(List.of(Element.<OrderDetails>builder()
                                         .value(OrderDetails.builder()
                                                    .otherDetails(OtherOrderDetails.builder().orderCreatedDate("").build())
                                                    .orderType("Test").build())
                                         .build()))
            .build();
        caseData.put("serviceOfApplicationHeader","TestHeader");
        caseData.put("option1","1");
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);

        when(serviceOfApplicationService.getCollapsableOfSentDocuments()).thenReturn("Collapsable");
        List<String> createdOrders = new ArrayList<>();
        createdOrders.add("Standard directions order");
        String courtEmail = "test1@test.com";
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(any())).thenReturn(courtEmail);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(serviceOfApplicationService.getOrderSelectionsEnumValues(Mockito.anyList(), Mockito.anyMap())).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        assertExpectedException(() -> {
            serviceOfApplicationController
            .handleAboutToStart(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForHandleAboutToSubmit() throws Exception {
        CaseData cd = CaseData.builder()
            .caseInvites(Collections.emptyList())
            .build();

        Map<String, Object> caseData = new HashMap<>();
        final CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(cd,  Map.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            serviceOfApplicationController.handleSubmitted(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
