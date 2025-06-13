package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityC8RefugeService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.FL401SubmitApplicationService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1;

@ExtendWith(MockitoExtension.class)
class ResubmitApplicationControllerTest {

    @InjectMocks
    private ResubmitApplicationController resubmitApplicationController;

    @Mock
    private CaseEventService caseEventService;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private UserService userService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private Court court;

    @Mock
    private ConfidentialityTabService confidentialityTabService;

    @Mock
    private ConfidentialityC8RefugeService confidentialityC8RefugeService;

    @Mock
    private FL401SubmitApplicationService fl401SubmitApplicationService;

    @Mock
    private EventService eventPublisher;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private CaseData caseDataSubmitted;
    private CaseData caseDataIssued;

    private CaseData caseDataGateKeeping;

    private CaseData caseDataGateKeepingMiam;

    private CaseData caseDataReSubmitted;

    private AllegationOfHarm allegationOfHarm;
    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";
    public static final String CURRENT_DATE = DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.now(ZoneId.of("Europe/London")));
    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;
    @Mock
    MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;
    @Mock
    SystemUserService systemUserService;

    @BeforeEach
    void init() {
        List<MiamExemptionsChecklistEnum> listMiamExemptionsChecklistEnum = new ArrayList<>();
        listMiamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.mpuDomesticAbuse);
        listMiamExemptionsChecklistEnum.add(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance);

        DomesticAbuseEvidenceDocument domesticAbuseEvidenceDocument = DomesticAbuseEvidenceDocument.builder()
            .domesticAbuseDocument(Document.builder().documentFileName("test").categoryId("test").build()).build();

        Element<DomesticAbuseEvidenceDocument> domesticAbuseEvidenceDocumentVal = Element
            .<DomesticAbuseEvidenceDocument>builder().value(domesticAbuseEvidenceDocument).build();

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
            .mpuExemptionReasons(listMiamExemptionsChecklistEnum)
            .mpuDomesticAbuseEvidences(List.of(MiamDomesticAbuseChecklistEnum.miamDomesticAbuseChecklistEnum_Value_1))
            .mpuIsDomesticAbuseEvidenceProvided(YesOrNo.Yes)
            .mpuPreviousMiamAttendanceReason(miamPolicyUpgradePreviousAttendance_Value_1)
            .mpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum.miamCertificate)
            .mpuDocFromDisputeResolutionProvider(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuCertificateByMediator(Document.builder().documentFileName("Confidential_test").categoryId("test").documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15").documentBinaryUrl("https:google.com").build())
            .mpuDomesticAbuseEvidenceDocument(List.of(domesticAbuseEvidenceDocumentVal))
            .build();

        allegationOfHarm = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();
        caseData = CaseData.builder()
            .id(12345L)
            .courtEmailAddress("test@email.com")
            .allegationOfHarm(allegationOfHarm)
            .courtName("test court")
            .courtId("123")
            .build();

        caseDataGateKeepingMiam = CaseData.builder()
            .id(12345L)
            .courtEmailAddress("test@email.com")
            .allegationOfHarm(allegationOfHarm)
            .miamPolicyUpgradeDetails(miamPolicyUpgradeDetails)
            .caseTypeOfApplication("C100")
            .courtName("test court")
            .courtId("123")
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();


        caseDataSubmitted = CaseData.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID)
            .allegationOfHarm(allegationOfHarm)
            .dateSubmitted(CURRENT_DATE)
            .courtId("123")
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

        caseDataReSubmitted = CaseData.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID)
            .allegationOfHarm(allegationOfHarm)
            .dateSubmitted(CURRENT_DATE)
            .courtId("123")
            .courtName("test court")
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .state("AWAITING_RESUBMISSION_TO_HMCTS")
            .data(Map.of("caseTypeOfApplication", "C100"))
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(caseDetails)
            .caseDetails(caseDetails)
            .build();

        court = Court.builder()
            .courtName("test court")
            .countyLocationCode(123)
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    void whenLastEventWasSubmitted_thenSubmittedPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataSubmitted);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataSubmitted.getId()))).thenReturn(caseEvents);
        when(courtFinderService.getNearestFamilyCourt(caseDataSubmitted)).thenReturn(court);

        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(State.SUBMITTED_PAID, response.getData().get("state"));
        verify(allTabService).getAllTabsFields(caseDataReSubmitted);

    }

    @Test
    void whenLastEventWasSubmitted_thenSubmittedPathFollowed1() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataGateKeepingMiam);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataSubmitted.getId()))).thenReturn(caseEvents);
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(),any())).thenReturn(caseDataGateKeepingMiam);
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(any(),any())).thenReturn(caseDataGateKeepingMiam);
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(State.SUBMITTED_PAID, response.getData().get("state"));
    }

    @Test
    void whenLastEventWasGateKeeping_thenGatekeepingPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8, "test",
                               DOCUMENT_FIELD_C1A, "test",
                               DOCUMENT_FIELD_FINAL, "test"
            ));
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(State.JUDICIAL_REVIEW, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(allTabService).getAllTabsFields(caseDataGateKeeping);

    }

    @Test
    void givenAllegationsOfHarmAndEnglish_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8, "test",
                               DOCUMENT_FIELD_C1A, "test",
                               DOCUMENT_FIELD_FINAL, "test"
            ));
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

    @Test
    void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
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
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(allTabService).getAllTabsFields(caseDataNoAllegations);

    }

    @Test
    void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGeneratedForAllegationOfHarmRevised()
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
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
        );

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(allTabService).getAllTabsFields(caseDataNoAllegations);

    }



    @Test
    void givenAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUED.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test",
                               DOCUMENT_FIELD_FINAL_WELSH, "test",
                               DOCUMENT_FIELD_C1A_WELSH, "test"
            ));

        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(State.CASE_ISSUED, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

    @Test
    void testResubmitForFl401() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController
            .fl401resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertTrue(response.getData().containsKey("isNotificationSent"));
        assertTrue(response.getData().containsKey(STATE_FIELD));
        assertTrue(response.getData().containsKey(CASE_DATE_AND_TIME_SUBMITTED_FIELD));
        assertTrue(response.getData().containsKey(DATE_SUBMITTED_FIELD));
        assertEquals(CURRENT_DATE, response.getData().get(DATE_SUBMITTED_FIELD));

    }

    @Test
    void testResubmitForFl401_whenGateKeeping() throws Exception {
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
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        assertTrue(response.getData().containsKey("applicantsConfidentialDetails"));
        assertTrue(response.getData().containsKey("childrenConfidentialDetails"));

    }

    @Test
    void resubmitApplicationConfidentUpdateConfidentialityTabService() throws Exception {
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
        AboutToStartOrSubmitCallbackResponse response = resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        assertTrue(response.getData().containsKey("applicantsConfidentialDetails"));
        assertTrue(response.getData().containsKey("childrenConfidentialDetails"));
    }

    @Test
    void testFl401resubmitApplication() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.JUDICIAL_REVIEW.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response =
            resubmitApplicationController.fl401resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        assertTrue(response.getData().containsKey("fl401ConfidentialityCheckResubmit"));
    }

    @Test
    void testExceptionForResubmitApplicationConfidentUpdateConfidentialityTabService() {

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            resubmitApplicationController.resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForFl401resubmitApplication() {
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            resubmitApplicationController.fl401resubmitApplication(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }
}
