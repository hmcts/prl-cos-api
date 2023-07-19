package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResubmitApplicationControllerTest {

    @InjectMocks
    ResubmitApplicationController resubmitApplicationController;

    @Mock
    CaseEventService caseEventService;

    @Mock
    SolicitorEmailService solicitorEmailService;

    @Mock
    CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    OrganisationService organisationService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DocumentGenService documentGenService;

    @Mock
    UserService userService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private Court court;

    @Mock
    private ConfidentialityTabService confidentialityTabService;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private CaseData caseDataSubmitted;
    private CaseData caseDataIssued;

    private CaseData caseDataGateKeeping;
    private AllegationOfHarm allegationOfHarm;
    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this);

        allegationOfHarm = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();
        caseData = CaseData.builder()
            .id(12345L)
            .courtEmailAddress("test@email.com")
            .allegationOfHarm(allegationOfHarm)
            .courtName("testcourt")
            .courtId("123")
            .build();
        caseDataSubmitted = CaseData.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID)
            .allegationOfHarm(allegationOfHarm)
            .build();

        caseDataIssued = CaseData.builder()
            .id(12345L)
            .state(State.CASE_ISSUED)
            .allegationOfHarm(allegationOfHarm)
            .build();

        caseDataGateKeeping = CaseData.builder()
            .id(12345L)
            .state(State.JUDICIAL_REVIEW)
            .allegationOfHarm(allegationOfHarm)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("caseTypeOfApplication", "C100"))
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails prlCaseDetailsSubmitted = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .caseData(caseDataSubmitted)
            .build();

        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails prlCaseDetailsIssued = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .caseData(caseDataIssued)
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .countyLocationCode(123)
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void whenLastEventWasSubmitted_thenSubmittedPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataSubmitted);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataSubmitted.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);

        assertEquals(State.SUBMITTED_PAID, response.getData().get("state"));
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendReSubmitEmail(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataSubmitted);

    }

    @Test
    public void whenLastEventWasGatkepping_thenGatekeepingPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenEng(true)
            .isGenWelsh(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8, "test",
                               DOCUMENT_FIELD_C1A, "test",
                               DOCUMENT_FIELD_FINAL, "test"
            ));
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);

        assertEquals(State.JUDICIAL_REVIEW, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataGateKeeping);

    }

    @Test
    public void givenAllegationsOfHarmAndEnglish_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenEng(true)
            .isGenWelsh(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8, "test",
                               DOCUMENT_FIELD_C1A, "test",
                               DOCUMENT_FIELD_FINAL, "test"
            ));
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

    @Test
    public void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        AllegationOfHarm allegationOfHarmNo = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(No).build();

        CaseData caseDataNoAllegations = CaseData.builder()
            .id(12345L)
            .state(State.CASE_ISSUED)
            .allegationOfHarm(allegationOfHarmNo)
            .build();

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenWelsh(true)
            .isGenEng(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataNoAllegations);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataNoAllegations.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseDataNoAllegations)).thenReturn(court);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            caseDataNoAllegations);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            caseDataNoAllegations);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test", DOCUMENT_FIELD_FINAL_WELSH, "test"
            ));


        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataNoAllegations);

    }

    @Test
    public void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGeneratedForAllegationOfHarmRevised()
            throws Exception {
        AllegationOfHarmRevised allegationOfHarmNo = AllegationOfHarmRevised.builder()
                .newAllegationsOfHarmYesNo(No).build();

        CaseData caseDataNoAllegations = CaseData.builder()
                .id(12345L)
                .state(State.CASE_ISSUED)
                .allegationOfHarmRevised(allegationOfHarmNo)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();

        List<CaseEventDetail> caseEvents = List.of(
                CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
                CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
                CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
                CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
                CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
                .isGenWelsh(true)
                .isGenEng(false)
                .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataNoAllegations);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataNoAllegations.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseDataNoAllegations)).thenReturn(court);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
                caseDataNoAllegations);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
                caseDataNoAllegations);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
                .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test", DOCUMENT_FIELD_FINAL_WELSH, "test"
                ));


        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
        );

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataNoAllegations);

    }



    @Test
    public void givenAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenWelsh(true)
            .isGenEng(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test",
                               DOCUMENT_FIELD_FINAL_WELSH, "test",
                               DOCUMENT_FIELD_C1A_WELSH, "test"
            ));

        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

    @Test
    public void testResubmitForFl401() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        UserDetails userDetails = UserDetails.builder().build();

        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController
            .fl401resubmitApplication(authToken, s2sToken, callbackRequest);

        verify(solicitorEmailService).sendEmailToFl401Solicitor(caseDetails, userDetails);
        verify(caseWorkerEmailService).sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());
        assertTrue(response.getData().containsKey("isNotificationSent"));
        assertTrue(response.getData().containsKey(STATE_FIELD));
        assertTrue(response.getData().containsKey(CASE_DATE_AND_TIME_SUBMITTED_FIELD));

    }

    @Test
    public void testResubmitForFl401_whenGateKeeping() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(confidentialityTabService.updateConfidentialityDetails(Mockito.any(CaseData.class))).thenReturn(Map.of(
            "applicantsConfidentialDetails",
            List.of(Element.builder().value(ApplicantConfidentialityDetails.builder().build())),
            "childrenConfidentialDetails",
            List.of(Element.builder().value(ChildConfidentialityDetails.builder().build()))
        ));
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);
        assertTrue(response.getData().containsKey("applicantsConfidentialDetails"));
        assertTrue(response.getData().containsKey("childrenConfidentialDetails"));

    }

    @Test
    public void resubmitApplicationConfidentUpdateConfidentialityTabService() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(confidentialityTabService.updateConfidentialityDetails(Mockito.any(CaseData.class))).thenReturn(Map.of(
            "applicantsConfidentialDetails",
            List.of(Element.builder().value(ApplicantConfidentialityDetails.builder().build())),
            "childrenConfidentialDetails",
            List.of(Element.builder().value(ChildConfidentialityDetails.builder().build()))
        ));
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);
        assertTrue(response.getData().containsKey("applicantsConfidentialDetails"));
        assertTrue(response.getData().containsKey("childrenConfidentialDetails"));
    }

    @Test
    public void testFl401resubmitApplication() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(confidentialityTabService.updateConfidentialityDetails(Mockito.any(CaseData.class))).thenReturn(Map.of(
            "applicantsConfidentialDetails",
            List.of(Element.builder().value(ApplicantConfidentialityDetails.builder().build())),
            "childrenConfidentialDetails",
            List.of(Element.builder().value(ChildConfidentialityDetails.builder().build()))
        ));
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.fl401resubmitApplication(authToken, s2sToken, callbackRequest);
        assertTrue(response.getData().containsKey("fl401ConfidentialityCheckResubmit"));
    }

    @Test
    public void testExceptionForResubmitApplicationConfidentUpdateConfidentialityTabService() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(confidentialityTabService.updateConfidentialityDetails(Mockito.any(CaseData.class))).thenReturn(Map.of(
            "applicantsConfidentialDetails",
            List.of(Element.builder().value(ApplicantConfidentialityDetails.builder().build())),
            "childrenConfidentialDetails",
            List.of(Element.builder().value(ChildConfidentialityDetails.builder().build()))
        ));
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            resubmitApplicationController.resubmitApplication(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForFl401resubmitApplication() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(confidentialityTabService.updateConfidentialityDetails(Mockito.any(CaseData.class))).thenReturn(Map.of(
            "applicantsConfidentialDetails",
            List.of(Element.builder().value(ApplicantConfidentialityDetails.builder().build())),
            "childrenConfidentialDetails",
            List.of(Element.builder().value(ChildConfidentialityDetails.builder().build()))
        ));
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            resubmitApplicationController.fl401resubmitApplication(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
