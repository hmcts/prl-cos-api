package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.handlers.CaseEventHandler;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.ReturnApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;


@RunWith(MockitoJUnitRunner.class)
public class ReturnApplicationReturnMessageControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ReturnApplicationReturnMessageController returnApplicationReturnMessageController;

    @Mock
    private UserService userService;

    @Mock
    private ReturnApplicationService returnApplicationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private EventService eventPublisher;

    @Mock
    private AllTabServiceImpl allTabsService;

    @Mock
    private CaseEventHandler caseEventHandler;

    private CallbackRequest callbackRequest;

    @Mock
    MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;

    CaseData casedata;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();

        casedata = CaseData.builder()
            .applicantCaseName("TestCase")
            .id(123L)
            .applicants(applicantList)
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void shouldStartReturnApplicationReturnMessageWithCaseDetails() throws Exception {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("returnMessage", "Test");

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)).thenReturn(casedata);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = returnApplicationReturnMessageController
            .returnApplicationReturnMessage(
            authToken,
            s2sToken,
            callbackRequest);
        verify(userService).getUserDetails(authToken);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void testReturnApplicationEmailNotification() throws Exception {

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .applicants(applicantList)
            .applicantSolicitorEmailAddress("testing@test.com")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(returnApplicationService.updateMiamPolicyUpgradeDataForConfidentialDocument(any(CaseData.class), anyMap())).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseEventHandler.getUpdatedTaskList(any(CaseData.class))).thenReturn("taskList");
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();


        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            returnApplicationReturnMessageController.returnApplicationEmailNotification(authToken, s2sToken, callbackRequest);

        verify(allTabsService, times(1)).getAllTabsFields(any(CaseData.class));
    }

    @Test
    public void testExceptionForReturnApplicationReturnMessage() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("returnMessage", "Test");

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            returnApplicationReturnMessageController
                .returnApplicationReturnMessage(
                    authToken,
                    s2sToken,
                    callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForReturnApplicationEmailNotification() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .applicants(applicantList)
            .applicantSolicitorEmailAddress("testing@test.com")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            returnApplicationReturnMessageController.returnApplicationEmailNotification(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testReturnApplicationEmailNotificationMiam() throws Exception {

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
        MiamPolicyUpgradeDetails miamPolicyUpgradeDetails = MiamPolicyUpgradeDetails
            .builder()
            .mpuChildInvolvedInMiam(YesOrNo.Yes)
            .mpuApplicantAttendedMiam(YesOrNo.Yes)
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mediatorRegistrationNumber("123")
            .familyMediatorServiceName("test")
            .soleTraderName("test")
            .miamCertificationDocumentUpload(Document.builder().build())
            .mpuClaimingExemptionMiam(YesOrNo.Yes)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .courtEmailAddress("test@email.com")
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails)
            .caseTypeOfApplication("C100")
            .courtName("testcourt")
            .courtId("123")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(returnApplicationService.updateMiamPolicyUpgradeDataForConfidentialDocument(any(CaseData.class), anyMap())).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseEventHandler.getUpdatedTaskList(any(CaseData.class))).thenReturn("taskList");
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();


        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            returnApplicationReturnMessageController.returnApplicationEmailNotification(authToken, s2sToken, callbackRequest);

        verify(allTabsService, times(1)).getAllTabsFields(any(CaseData.class));
    }
}
