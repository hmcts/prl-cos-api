package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.AmendCourtService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.C100IssueCaseService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.GatekeepingDetailsService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.json.JsonValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRUE;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V2_FLAG;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.TASK_LIST_V3_FLAG;
import static uk.gov.hmcts.reform.prl.enums.Event.SOLICITOR_CREATE;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_RESTRICTED_FLAG;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_TRIGGERED_BY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class CallbackControllerTest {
    public static final String SOLICITOR_EMAIL = "unknown@test.com";
    @Mock
    private ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    @Mock
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @InjectMocks
    private CallbackController callbackController;

    @Mock
    private UserService userService;

    @Mock
    CourtSealFinderService courtSealFinderService;

    @Mock
    private GatekeepingDetailsService gatekeepingDetailsService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    AmendCourtService amendCourtService;

    @Mock
    private WorkflowResult workflowResult;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    @Mock
    private DgsService dgsService;

    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    ManageDocumentsService manageDocumentsService;

    @Mock
    UpdatePartyDetailsService updatePartyDetailsService;

    @Mock
    SendgridService sendgridService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    C100JsonMapper c100JsonMapper;

    @Mock
    private CaseEventService caseEventService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CourtFinderService courtLocatorService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private ConfidentialityTabService confidentialityTabService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private ApplicantsListGenerator applicantsListGenerator;

    @Mock
    private PaymentRequestService paymentRequestService;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private  EventService eventPublisher;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    private static final Map<String, Object> c100DraftMap = new HashMap<>();
    private static final Map<String, Object> c100DocsMap = new HashMap<>();

    private static final Map<String, Object> fl401DraftMap = new HashMap<>();
    private static final Map<String, Object> fl401DocsMap = new HashMap<>();
    private CourtVenue courtVenue;

    @Before
    public void setUp() {

        courtVenue = CourtVenue.builder()
            .courtName("test")
            .regionId("1")
            .siteName("test")
            .region("test")
            .build();
        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .roles(ROLES)
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        c100DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_FIELD, "test");
        c100DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD, "test");

        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C1A, "test");
        c100DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_C1A_WELSH, "test");
        c100DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");

        fl401DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_FIELD, "test");
        fl401DraftMap.put(PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD, "test");

        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(c100IssueCaseService.getFactCourtId(courtVenue)).thenReturn("123");
    }

    @Test
    public void testConfirmMiamApplicationOrExemption() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        when(applicationConsiderationTimetableValidationWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        callbackController.validateApplicationConsiderationTimetable(authToken, s2sToken, callbackRequest);

        verify(applicationConsiderationTimetableValidationWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(applicationConsiderationTimetableValidationWorkflow);

    }

    @Test
    public void testValidateApplicationConsiderationTimetable() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        when(validateMiamApplicationOrExemptionWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        callbackController.validateMiamApplicationOrExemption(authToken, s2sToken, callbackRequest);

        verify(validateMiamApplicationOrExemptionWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(validateMiamApplicationOrExemptionWorkflow);

    }

    @Test
    public void testGenerateAndStoreDocument() throws Exception {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.occupationOrder);
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("c100DraftFilename.pdf")
                               .build())
            .id(123L)
            .draftOrderDocWelsh(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c100DraftWelshFilename")
                                    .build())
            .applicants(applicantList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build())
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            c100DraftMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_FIELD));
        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_WELSH_FIELD));
    }

    @Test
    public void testGenerateAndStoreDocumentForFL401WithBothOrders() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.occupationOrder);
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("PRL-DRAFT-C100-20.docx")
                               .build())
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(YesOrNo.Yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            fl401DraftMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_FIELD));
        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_WELSH_FIELD));
    }

    @Test
    public void testGenerateAndStoreDocumentForFL401WithNonMolestationOrders() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("PRL-DRAFT-C100-20.docx")
                               .build())
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());


        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            fl401DraftMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_FIELD));
        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_WELSH_FIELD));
    }

    @Test
    public void testGenerateAndStoreDocumentForFL401WithOccupationOrders() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.occupationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("PRL-DRAFT-C100-20.docx")
                               .build())
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();


        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            fl401DraftMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_FIELD));
        assertTrue(response.getData().containsKey(DRAFT_DOCUMENT_WELSH_FIELD));
    }

    @Test
    public void updateApplicationTest() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        callbackController.updateApplication(authToken, s2sToken, callbackRequest);

        verify(allTabsService, times(1)).updateAllTabsIncludingConfTab(anyString());
    }

    @Test
    public void testSendCaseWithdrawNotification() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.Yes)
            .withDrawApplicationReason("Test data")
            .build();

        PartyDetails applicant = PartyDetails.builder().solicitorEmail("test@gmail.com").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
        CaseData caseData = CaseData.builder()
            .localCourtAdmin(List.of(Element.<LocalCourtAdminEmail>builder()
                                         .value(LocalCourtAdminEmail
                                                    .builder().email("test@gmail.com")
                                                    .build()).build()))
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(withdrawApplication)
            .applicants(applicantList)
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(ISSUED_STATE).build()));
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .state(ISSUED_STATE)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testSendCaseWithdrawNotificationNotInCaseIssuedState() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.Yes)
            .withDrawApplicationReason("Test data")
            .build();

        sendEmail(SOLICITOR_EMAIL, withdrawApplication, 1);
    }

    @Test
    public void testUpdateApplicantAndChildNames() throws Exception {

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("test1")
            .lastName("test22")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicantList)
            .children(listOfChildren)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        Map<String, Object> caseDetailsUpdatedwithName = new HashMap<>();
        caseDetailsUpdatedwithName.put("applicantName", "test1 test22");
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(caseDataUpdated).build()).build();
        when(objectMapper.convertValue(caseDataUpdated, CaseData.class)).thenReturn(caseData);
        when(updatePartyDetailsService.updateApplicantRespondentAndChildData(callbackRequest,authToken)).thenReturn(
            caseDetailsUpdatedwithName);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.updatePartyDetails(
                authToken,
                s2sToken,
                callbackRequest
            );
        Map<String, Object> caseDetailsResponse = aboutToStartOrSubmitCallbackResponse.getData();
        assertEquals("test1 test22", caseDetailsResponse.get("applicantName"));
    }

    @Test
    public void testSendCaseWithdrawNotificationNo() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.No)
            .withDrawApplicationReason("Test data No")
            .build();
        sendEmail(SOLICITOR_EMAIL, withdrawApplication, 0);
    }

    private void sendEmail(String solicitorEmail, WithdrawApplication withdrawApplication, int timesCalled) {
        PartyDetails applicant = PartyDetails.builder().solicitorEmail(solicitorEmail).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
        CaseData caseData = CaseData.builder()
            .localCourtAdmin(List.of(Element.<LocalCourtAdminEmail>builder()
                                         .value(LocalCourtAdminEmail
                                                    .builder().email("test@gmail.com")
                                                    .build()).build()))
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(withdrawApplication)
            .applicants(applicantList).build();
        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(SUBMITTED_STATE).build()));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .state(SUBMITTED_STATE)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken,s2sToken, callbackRequest));
    }

    @Test
    public void testSendCaseWithNullWithdrawApplication() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(null)
            .build();
        CaseData caseData = CaseData.builder()
            .withDrawApplicationData(withdrawApplication)
            .build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testWithdrawNotificationWithSelectionNo() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.No)
            .build();
        CaseData caseData = CaseData.builder()
            .withDrawApplicationData(withdrawApplication)
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(SUBMITTED_STATE).build()));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                             .data(stringObjectMap).build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken, s2sToken,callbackRequest));
    }

    @Test
    public void testSendToGateKeeperNotification() throws Exception {

        PartyDetails applicant1 = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.No)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String isConfidential = "No";
        if (applicant1.hasConfidentialInfo() || child.hasConfidentialInfo()) {
            isConfidential = "Yes";
        }

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .isCaseUrgent(YesOrNo.No)
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        assertNotNull(callbackRequest);

    }

    @Test
    public void resendNotificationtoRpaTest() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(1234L)
            .build();
        Map<String, Object> json = new HashMap<>();
        json.put("id", 1234L);

        when(objectMapper.convertValue(json, CaseData.class)).thenReturn(caseData);
        when(c100JsonMapper.map(caseData)).thenReturn(JsonValue.EMPTY_JSON_OBJECT);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest
            .builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1234L)
                             .data(json)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        callbackController.resendNotificationToRpa(authToken, s2sToken, callbackRequest);
        verify(sendgridService, times(1)).sendEmail(JsonValue.EMPTY_JSON_OBJECT);
    }

    @Test
    public void testCopyFL401CasenameToC100CaseName() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantOrRespondentCaseName", "test");
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseDetails.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("abcd").applicantOrganisationPolicy(
            applicantOrganisationPolicy).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken,s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void testC100CaseCreationWhenOrPolicyNotPresentInCaseData() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantOrRespondentCaseName", "test");
        caseDetails.put(CASE_TYPE_OF_APPLICATION, C100_CASE_TYPE);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("abcd").build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(SOLICITOR_CREATE.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .caseTypeId(C100_CASE_TYPE)
                             .data(caseDetails).build()).build();
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V2_FLAG)).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken,s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void testCreateCaseC100ForAllegationHarmRevised() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantOrRespondentCaseName", "test");
        caseDetails.put("caseTypeOfApplication", "C100");
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseDetails.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("abcd")
                .taskListVersion(TASK_LIST_VERSION_V2)
            .applicantOrganisationPolicy(
            applicantOrganisationPolicy).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken,s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }


    @Test
    public void aboutToSubmitCaseCreationToC100ForNullCaseName() {

        Map<String, Object> caseData = new HashMap<>();
        Organisations org = Organisations.builder().name("testOrg")
            .organisationIdentifier("abcd")
            .build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseData.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        CaseData caseDataUpdated = CaseData.builder().id(123L)
            .applicantCaseName("abcd").applicantOrganisationPolicy(applicantOrganisationPolicy).build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseDataUpdated);
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(false);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void testAddCaseNumberSubmitted() throws Exception {

        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(ISSUED_STATE).build()));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .addCaseNumberSubmitted(authToken, s2sToken, callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("issueDate"));
        assertEquals(No,aboutToStartOrSubmitCallbackResponse.getData().get("isAddCaseNumberAdded"));
    }

    @Test
    public void testAddCaseNumberStateSubmittedPaid() throws Exception {

        CaseData caseData = CaseData.builder()
                .issueDate(LocalDate.now())
                .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(caseEventService.findEventsForCase("1"))
                .thenReturn(List.of(CaseEventDetail.builder().stateId(SUBMITTED_STATE).build()));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(1L)
                        .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
                .addCaseNumberSubmitted(authToken, s2sToken, callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("issueDate"));
        assertEquals(Yes,aboutToStartOrSubmitCallbackResponse.getData().get("isAddCaseNumberAdded"));
    }

    @Test
    public void aboutToSubmitCaseCreationToC100ForNullCaseNameWithException() {

        Map<String, Object> caseData = new HashMap<>();
        Organisations org = Organisations.builder().name("testOrg")
            .organisationIdentifier("abcd")
            .build();
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(null);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        CaseData caseDataUpdated = CaseData.builder().id(123L)
            .applicantCaseName("abcd").applicantOrganisationPolicy(applicantOrganisationPolicy).build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseDataUpdated);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().roles(List.of(
            CITIZEN_ROLE)).build());
        doNothing().when(ccdDataStoreService).removeCreatorRole(anyString(), anyString());
        when(authTokenGenerator.generate()).thenReturn("Generate");
        doNothing().when(assignCaseAccessClient)
            .assignCaseAccess(anyString(), anyString(), anyBoolean(), any());
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void testCopyManageDocsOnSubmitWithNullData() {

        Map<String, Object> caseData = new HashMap<>();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        CaseData caseData1 = CaseData.builder().build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .copyManageDocsForTabs(authToken, s2sToken, callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("furtherEvidences"));
    }

    @Test
    public void testCopyManageDocsOnSubmit() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        CaseData caseData1 = CaseData.builder()
            .documentCategoryChecklist(DocumentCategoryEnum.documentCategoryChecklistEnumValue2)
            .furtherEvidences(List.of(Element.<FurtherEvidence>builder()
                                          .value(FurtherEvidence.builder()
                                                     .typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.consentOrder)
                                                     .restrictCheckboxFurtherEvidence(List.of(RestrictToCafcassHmcts.restrictToGroup))
                                                     .build())
                                          .build()))
            .correspondence(List.of(Element.<Correspondence>builder()
                                        .value(Correspondence.builder()
                                                   .restrictCheckboxCorrespondence(List.of(RestrictToCafcassHmcts.restrictToGroup))
                                                   .build())
                                        .build()))
            .otherDocuments(List.of(Element.<OtherDocuments>builder()
                                        .value(OtherDocuments.builder()
                                                   .documentTypeOther(DocTypeOtherDocumentsEnum.otherReports)
                                                   .restrictCheckboxOtherDocuments(List.of(RestrictToCafcassHmcts.restrictToGroup))
                                                   .build())
                                        .build()))
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .copyManageDocsForTabs(authToken, s2sToken, callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("legalProfQuarantineDocsList"));
    }

    @Test
    public void testSendCaseWithdrawNotificationForFL401() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(YesOrNo.Yes)
            .withDrawApplicationReason("Test data")
            .build();

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@solicitor.com")
            .build();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() :
            userDetails.getEmail();

        CaseData caseData = CaseData.builder()
            .courtEmailAddress("test@gmail.com")
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .withDrawApplicationData(withdrawApplication)
            .applicantsFL401(fl401Applicant)
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(ISSUED_STATE).build()));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .state(ISSUED_STATE)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken,s2sToken, callbackRequest));

    }

    @Test
    public void testSendCaseWithdrawNotificationForFL401NoWithdrawApplication() throws Exception {
        WithdrawApplication withdrawApplication = WithdrawApplication.builder()
            .withDrawApplication(Yes)
            .withDrawApplicationReason("Test data")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        Map<String, Object> data = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(1L)
            .courtEmailAddress("test@gmail.com")
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .withDrawApplicationData(withdrawApplication)
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(caseEventService.findEventsForCase("1"))
            .thenReturn(List.of(CaseEventDetail.builder().stateId(SUBMITTED_STATE).build()));
        when(caseEventService.findEventsForCase(any(String.class)))
            .thenReturn(List.of(CaseEventDetail.builder().stateId("CLOSED").build()));

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(callbackController.caseWithdrawAboutToSubmit(authToken,s2sToken, callbackRequest));
    }

    @Test
    public void testGenerateDocumentSubmitApplicationWithC8() throws Exception {
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .helpWithFees(No)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build())
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .childrenConfidentialDetails(List.of(element(ChildConfidentialityDetails.builder().build())))
            .id(123L)
            .build();

        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(), anyMap())).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("testAuth");
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(any(), anyString())).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .id(123L)
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("c100C8Template")
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("c100C1aTemplate")
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("test")
                                         .build())
                      .c8WelshDocument(Document.builder()
                                           .documentUrl(generatedDocumentInfo.getUrl())
                                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                           .documentHash(generatedDocumentInfo.getHashToken())
                                           .documentFileName("test")
                                           .build())
                      .c1AWelshDocument(Document.builder()
                                            .documentUrl(generatedDocumentInfo.getUrl())
                                            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                            .documentHash(generatedDocumentInfo.getHashToken())
                                            .documentFileName("test")
                                            .build())
                      .finalWelshDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("test")
                                              .build())
                      .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c8Document", "document",
                   "c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document"
            )
        );
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
            PaymentServiceResponse.builder().serviceRequestReference("1234").build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
            );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8Document"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
    }

    @Test
    public void testGenerateDocumentSubmitApplicationWithC8ForAllegationOfHarmRevised() throws Exception {
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().mpuClaimingExemptionMiam(Yes).build())
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(Yes)
                                  .newAllegationsOfHarmChildAbductionYesNo(Yes)
                                  .newAllegationsOfHarmChildAbuseYesNo(Yes)
                                  .newAllegationsOfHarmDomesticAbuseYesNo(Yes).build())
                .taskListVersion(TASK_LIST_VERSION_V2)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .childrenConfidentialDetails(List.of(element(ChildConfidentialityDetails.builder().build())))
            .id(123L)
            .build();

        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(), anyMap())).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("testAuth");
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(any(), anyString())).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
                PaymentServiceResponse.builder().serviceRequestReference("1234").build());

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .id(123L)
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("c100C8Template")
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("c100C1aTemplate")
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("test")
                                         .build())
                      .c8WelshDocument(Document.builder()
                                           .documentUrl(generatedDocumentInfo.getUrl())
                                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                           .documentHash(generatedDocumentInfo.getHashToken())
                                           .documentFileName("test")
                                           .build())
                      .c1AWelshDocument(Document.builder()
                                            .documentUrl(generatedDocumentInfo.getUrl())
                                            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                            .documentHash(generatedDocumentInfo.getHashToken())
                                            .documentFileName("test")
                                            .build())
                      .finalWelshDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("test")
                                              .build())
                      .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c8Document", "document",
                   "c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document"
            )
        );

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                    s2sToken,
                callbackRequest
            );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8Document"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
    }


    @Test
    public void testGenerateDocumentSubmitApplicationConditionalFailures() throws Exception {

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();
        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                           .builder()
                           .firstName("Test")
                           .lastName("Name")
                           .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                               OtherPersonConfidentialityDetails.builder()
                                   .firstName("Confidential First Name")
                                   .lastName("Confidential Last Name")
                                   .relationshipToChildDetails("test")
                                   .address(address)
                                   .build()).build()))
                           .build()).build());
        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .applicants(applicantList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                .mpuClaimingExemptionMiam(Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuDomesticAbuse)).build())
            .consentOrder(Yes)
            .id(123L)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(false).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(confidentialityTabService.getConfidentialApplicantDetails(Mockito.any())).thenReturn(applicants);
        when(confidentialityTabService.getChildrenConfidentialDetails(caseData)).thenReturn(
            childConfidentialityDetails);
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
            PaymentServiceResponse.builder().serviceRequestReference("1234").build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(), anyMap())).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(caseData, s2sToken))
            .thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
            );
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8Document"));
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8WelshDocument"));
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
        verifyNoMoreInteractions(organisationService);
    }

    @Test
    public void testGenerateDocumentSubmitApplicationWithC8EmptyList() throws Exception {
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                           .builder()
                           .firstName("Test")
                           .lastName("Name")
                           .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                               OtherPersonConfidentialityDetails.builder()
                                   .firstName("Confidential First Name")
                                   .lastName("Confidential Last Name")
                                   .relationshipToChildDetails("test")
                                   .address(address)
                                   .build()).build()))
                           .build()).build());

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                .mpuClaimingExemptionMiam(Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuUrgency))
                .build())
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .build();

        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(), anyMap())).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("testAuth");
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(any(), anyString())).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .id(123L)
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("c100C8Template")
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("c100C1aTemplate")
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("test")
                                         .build())
                      .c8WelshDocument(Document.builder()
                                           .documentUrl(generatedDocumentInfo.getUrl())
                                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                           .documentHash(generatedDocumentInfo.getHashToken())
                                           .documentFileName("test")
                                           .build())
                      .c1AWelshDocument(Document.builder()
                                            .documentUrl(generatedDocumentInfo.getUrl())
                                            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                            .documentHash(generatedDocumentInfo.getHashToken())
                                            .documentFileName("test")
                                            .build())
                      .finalWelshDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("test")
                                              .build())
                      .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(confidentialityTabService.getConfidentialApplicantDetails(Mockito.any())).thenReturn(applicants);
        when(confidentialityTabService.getChildrenConfidentialDetails(caseData)).thenReturn(childConfidentialityDetails);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document"
            )
        );
        when(launchDarklyClient.isFeatureEnabled(TASK_LIST_V3_FLAG)).thenReturn(true);
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
            PaymentServiceResponse.builder().serviceRequestReference("1234").build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
            );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
    }


    @Test
    public void testGenerateDocumentSubmitApplicationWithC8EmptyListForAllegationOfHarmRevised() throws Exception {
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                           .builder()
                           .firstName("Test")
                           .lastName("Name")
                           .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                               OtherPersonConfidentialityDetails.builder()
                                   .firstName("Confidential First Name")
                                   .lastName("Confidential Last Name")
                                   .relationshipToChildDetails("test")
                                   .address(address)
                                   .build()).build()))
                           .build()).build());

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(Yes)
                                  .newAllegationsOfHarmChildAbductionYesNo(Yes)
                                  .newAllegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .newAllegationsOfHarmChildAbuseYesNo(Yes).build())
                .taskListVersion(TASK_LIST_VERSION_V2)
            .welshLanguageRequirement(Yes)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                .mpuClaimingExemptionMiam(Yes)
                .mpuExemptionReasons(List.of(MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance)).build())
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(Collections.emptyList())
            .childrenConfidentialDetails(Collections.emptyList())
            .id(123L)
            .build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
                PaymentServiceResponse.builder().serviceRequestReference("1234").build());

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .id(123L)
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("c100C8Template")
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("c100C1aTemplate")
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("test")
                                         .build())
                      .c8WelshDocument(Document.builder()
                                           .documentUrl(generatedDocumentInfo.getUrl())
                                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                           .documentHash(generatedDocumentInfo.getHashToken())
                                           .documentFileName("test")
                                           .build())
                      .c1AWelshDocument(Document.builder()
                                            .documentUrl(generatedDocumentInfo.getUrl())
                                            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                            .documentHash(generatedDocumentInfo.getHashToken())
                                            .documentFileName("test")
                                            .build())
                      .finalWelshDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("test")
                                              .build())
                      .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(systemUserService.getSysUserToken()).thenReturn(s2sToken);
        when(miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(caseData, s2sToken))
            .thenReturn(caseData);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(), anyMap())).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(confidentialityTabService.getConfidentialApplicantDetails(Mockito.any())).thenReturn(applicants);
        when(confidentialityTabService.getChildrenConfidentialDetails(Mockito.any(CaseData.class))).thenReturn(
            childConfidentialityDetails);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document"
            )
        );

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                    s2sToken,
                callbackRequest
            );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
    }


    @Test
    public void testPrePopulateCourtDetails() throws NotFoundException {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(Court.builder().build());
        when(courtLocatorService.getEmailAddress(Mockito.any(Court.class))).thenReturn(Optional.of(CourtEmailAddress.builder()
                                                                                                       .address("123@gamil.com").build()));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement.EMPTY));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  callbackController
            .prePopulateCourtDetails(authToken, s2sToken, callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("localCourtAdmin"));
    }

    @Test
    public void testPrePopulateCourtDetailsNotFound() throws NotFoundException {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(courtLocatorService.getNearestFamilyCourt(Mockito.any(CaseData.class))).thenReturn(Court.builder().build());
        when(courtLocatorService.getEmailAddress(Mockito.any(Court.class))).thenReturn(Optional.empty());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement.EMPTY));
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  callbackController
            .prePopulateCourtDetails(authToken,s2sToken,callbackRequest);
        Assertions.assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("localCourtAdmin"));
    }

    @Test
    public void testAmendCourtAboutToStart() throws Exception {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getCourtLocations(Mockito.anyString())).thenReturn(List.of(DynamicListElement.EMPTY));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  callbackController
            .amendCourtAboutToStart(authToken,s2sToken,callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtList"));
    }

    @Test
    public void testAmendCourtAboutToStartTransferCourt() throws Exception {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(Event.TRANSFER_TO_ANOTHER_COURT.getId())
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getFilteredCourtLocations(Mockito.anyString()))
            .thenReturn(List.of(DynamicListElement.EMPTY));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  callbackController
            .amendCourtAboutToStart(authToken,s2sToken,callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtList"));
    }

    @Test
    public void testAmendCourtAboutToSubmit() throws Exception {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("courtName", "testcourt");
        stringObjectMap.put("applicantCaseName", "test");
        stringObjectMap.put("caseTypeOfApplication", "C100_CASE_TYPE");
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("testName").courtName("test-court")
            .courtEmailAddress("testcourt@sdsd.com")
            .courtList(DynamicList.builder()
                           .value(DynamicListElement.builder().build()).build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =  callbackController
            .amendCourtAboutToSubmit(authToken,s2sToken,callbackRequest);
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("courtName"));
    }

    @Test
    public void testCaseNameHmctsInternalPresentForC100Case() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantCaseName", "test");
        caseDetails.put("caseTypeOfApplication", "C100_CASE_TYPE");
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseDetails.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("testName").applicantOrganisationPolicy(
            applicantOrganisationPolicy).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("caseNameHmctsInternal"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
    }

    @Test
    public void testCaseNameHmctsInternalPresentForFL401Case() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantOrRespondentCaseName", "test");
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseDetails.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("testName").applicantOrganisationPolicy(
            applicantOrganisationPolicy).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("caseNameHmctsInternal"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("applicantOrRespondentCaseName"));
    }

    @Test
    public void testSendToGatekeeperChanges() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();
        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                           .builder()
                           .firstName("Test")
                           .lastName("Name")
                           .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                               OtherPersonConfidentialityDetails.builder()
                                   .firstName("Confidential First Name")
                                   .lastName("Confidential Last Name")
                                   .relationshipToChildDetails("test")
                                   .address(address)
                                   .build()).build()))
                           .build()).build());
        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .applicants(applicantList)
            .consentOrder(Yes)
            .id(123L)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(false).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        final uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
                                                              .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        GatekeepingDetails gatekeepingDetails = GatekeepingDetails.builder()
            .isSpecificGateKeeperNeeded(Yes)
            .isJudgeOrLegalAdviserGatekeeping(SendToGatekeeperTypeEnum.judge)
            .judgeName(JudicialUser.builder()
                           .idamId("testId")
                           .personalCode("testCode")
                           .build())
            .build();
        when(gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap, null,
            refDataUserService)).thenReturn(gatekeepingDetails);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = callbackController.sendToGatekeeper(authToken,s2sToken,callbackRequest);
        assertEquals(SendToGatekeeperTypeEnum.judge,gatekeepingDetails.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(gatekeepingDetails,response.getData().get("gatekeepingDetails"));

    }

    @Test
    public void testSendToGatekeeperChangesJudgeIsNull() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();
        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                .value(ApplicantConfidentialityDetails.builder()
                    .firstName("ABC 1")
                    .lastName("XYZ 2")
                    .email("abc1@xyz.com")
                    .phoneNumber("09876543211")
                    .address(address)
                    .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                    .builder()
                    .firstName("Test")
                    .lastName("Name")
                    .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                        OtherPersonConfidentialityDetails.builder()
                            .firstName("Confidential First Name")
                            .lastName("Confidential Last Name")
                            .relationshipToChildDetails("test")
                            .address(address)
                            .build()).build()))
                    .build()).build());
        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .applicants(applicantList)
            .consentOrder(Yes)
            .id(123L)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(false).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        final uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap).build()).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
            .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        GatekeepingDetails gatekeepingDetails = GatekeepingDetails.builder()
            .isSpecificGateKeeperNeeded(Yes)
            .judgeName(JudicialUser.builder()
                .idamId("testId")
                .personalCode("testCode")
                .build())
            .build();
        when(gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap, null,
            refDataUserService)).thenReturn(gatekeepingDetails);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = callbackController.sendToGatekeeper(authToken,s2sToken,callbackRequest);
        assertEquals(gatekeepingDetails,response.getData().get("gatekeepingDetails"));

    }

    @Test
    public void testSendToGatekeeperChangesJudgeNameNull() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();
        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                .value(ApplicantConfidentialityDetails.builder()
                    .firstName("ABC 1")
                    .lastName("XYZ 2")
                    .email("abc1@xyz.com")
                    .phoneNumber("09876543211")
                    .address(address)
                    .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                    .builder()
                    .firstName("Test")
                    .lastName("Name")
                    .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                        OtherPersonConfidentialityDetails.builder()
                            .firstName("Confidential First Name")
                            .lastName("Confidential Last Name")
                            .relationshipToChildDetails("test")
                            .address(address)
                            .build()).build()))
                    .build()).build());
        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .applicants(applicantList)
            .consentOrder(Yes)
            .id(123L)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(false).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        final uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap).build()).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
            .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        GatekeepingDetails gatekeepingDetails = GatekeepingDetails.builder()
            .isSpecificGateKeeperNeeded(Yes)
            .isJudgeOrLegalAdviserGatekeeping(SendToGatekeeperTypeEnum.judge)
            .build();
        when(gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap, null,
            refDataUserService)).thenReturn(gatekeepingDetails);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = callbackController.sendToGatekeeper(authToken,s2sToken,callbackRequest);
        assertEquals(SendToGatekeeperTypeEnum.judge,gatekeepingDetails.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(gatekeepingDetails,response.getData().get("gatekeepingDetails"));

    }

    @Test
    public void testSendToGatekeeperChangesLegalAdvisorListNotNull() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();
        List<Element<ApplicantConfidentialityDetails>> applicants = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                .value(ApplicantConfidentialityDetails.builder()
                    .firstName("ABC 1")
                    .lastName("XYZ 2")
                    .email("abc1@xyz.com")
                    .phoneNumber("09876543211")
                    .address(address)
                    .build()).build());

        List<Element<ChildConfidentialityDetails>> childConfidentialityDetails = List.of(
            Element.<ChildConfidentialityDetails>builder()
                .value(ChildConfidentialityDetails
                    .builder()
                    .firstName("Test")
                    .lastName("Name")
                    .otherPerson(List.of(Element.<OtherPersonConfidentialityDetails>builder().value(
                        OtherPersonConfidentialityDetails.builder()
                            .firstName("Confidential First Name")
                            .lastName("Confidential Last Name")
                            .relationshipToChildDetails("test")
                            .address(address)
                            .build()).build()))
                    .build()).build());
        CaseData caseData = CaseData.builder()
            .children(listOfChildren)
            .applicants(applicantList)
            .consentOrder(Yes)
            .id(123L)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(false).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        final uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                .data(stringObjectMap).build()).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
            .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
            .code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        GatekeepingDetails gatekeepingDetails = GatekeepingDetails.builder()
            .isSpecificGateKeeperNeeded(Yes)
            .legalAdviserList(legalAdviserList)
            .isJudgeOrLegalAdviserGatekeeping(SendToGatekeeperTypeEnum.judge)
            .build();
        when(gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap, null,
            refDataUserService)).thenReturn(gatekeepingDetails);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = callbackController.sendToGatekeeper(authToken,s2sToken,callbackRequest);
        assertEquals(SendToGatekeeperTypeEnum.judge,gatekeepingDetails.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(gatekeepingDetails,response.getData().get("gatekeepingDetails"));

    }

    @Test
    public void testExceptionForValidateApplicationConsiderationTimetable() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.validateApplicationConsiderationTimetable(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForValidateMiamApplicationOrExemption() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.validateMiamApplicationOrExemption(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForGenerateAndStoreDocument() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.generateAndStoreDocument(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPrePopulateCourtDetails() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.prePopulateCourtDetails(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForGenerateDocumentSubmitApplication() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.generateDocumentSubmitApplication(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForAmendCourtAboutToStart() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            callbackController.amendCourtAboutToStart(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForAmendCourtAboutToSubmit() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.amendCourtAboutToSubmit(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForUpdateApplication() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.updateApplication(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForCaseWithdrawAboutToSubmit() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.caseWithdrawAboutToSubmit(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForSendToGatekeeper() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            callbackController.sendToGatekeeper(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForResendNotificationToRpa() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.resendNotificationToRpa(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForUpdatePartyDetails() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.updatePartyDetails(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForAboutToSubmitCaseCreation() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.aboutToSubmitCaseCreation(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForAddCaseNumberSubmitted() throws Exception {

        CaseData caseData = CaseData.builder().build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        when(amendCourtService.handleAmendCourtSubmission(Mockito.anyString(), Mockito.any(), Mockito.any()))
            .thenReturn(new HashMap<>());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        assertExpectedException(() -> {
            callbackController.addCaseNumberSubmitted(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }


    @Test
    public void testGenerateDocumentSubmitApplicationWithC8CourtAdmin() throws Exception {
        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .childLiveWith(Collections.singletonList(anotherPerson))
            .personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder().children(listOfChildren)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            .applicants(applicantList)
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbuseYesNo(Yes).build())
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .applicantsConfidentialDetails(List.of(element(ApplicantConfidentialityDetails.builder().build())))
            .childrenConfidentialDetails(List.of(element(ChildConfidentialityDetails.builder().build())))
            .id(123L)
            .build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .id(123L)
                      .c8Document(Document.builder()
                                      .documentUrl(generatedDocumentInfo.getUrl())
                                      .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                      .documentHash(generatedDocumentInfo.getHashToken())
                                      .documentFileName("c100C8Template")
                                      .build())
                      .c1ADocument(Document.builder()
                                       .documentUrl(generatedDocumentInfo.getUrl())
                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                       .documentHash(generatedDocumentInfo.getHashToken())
                                       .documentFileName("c100C1aTemplate")
                                       .build())
                      .finalDocument(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("test")
                                         .build())
                      .c8WelshDocument(Document.builder()
                                           .documentUrl(generatedDocumentInfo.getUrl())
                                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                           .documentHash(generatedDocumentInfo.getHashToken())
                                           .documentFileName("test")
                                           .build())
                      .c1AWelshDocument(Document.builder()
                                            .documentUrl(generatedDocumentInfo.getUrl())
                                            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                            .documentHash(generatedDocumentInfo.getHashToken())
                                            .documentFileName("test")
                                            .build())
                      .finalWelshDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("test")
                                              .build())
                      .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            Map.of("c8Document", "document",
                   "c1ADocument", "document",
                   "c1AWelshDocument", "document",
                   "finalWelshDocument", "document"
            )
        );
        when(paymentRequestService.createServiceRequestFromCcdCallack(Mockito.any(),Mockito.any())).thenReturn(
            PaymentServiceResponse.builder().serviceRequestReference("1234").build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            callbackController.generateDocumentSubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
            );
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c8Document"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1ADocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("c1AWelshDocument"));
        Assertions.assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("finalWelshDocument"));
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testTransferCourtSubmitEvent() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtList(DynamicList.builder().value(DynamicListElement.builder().code("test-test-test-test-test-test")
                                                       .build()).build())
            .courtCodeFromFact("123")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabsService.updateAllTabsIncludingConfTab(any())).thenReturn(callbackRequest.getCaseDetails());
        ResponseEntity<SubmittedCallbackResponse> responseEntity =  callbackController
            .transferCourtConfirmation(authToken, callbackRequest);
        Assertions.assertNotNull(responseEntity);
    }

    @Test
    public void testValidateCourtShouldNotGiveError() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtEmailAddress("email@test.com")
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt))
            .anotherCourt("test court").build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(amendCourtService.validateCourtFields(Mockito.any(),Mockito.any())).thenReturn(Boolean.FALSE);
        CallbackResponse response =  callbackController
            .validateCourtFields(callbackRequest);
        Assertions.assertNull(response.getErrors());
    }

    @Test
    public void testValidateCourtShouldGiveError() throws Exception {
        CaseData caseData = CaseData.builder()
            .cantFindCourtCheck(List.of(CantFindCourtEnum.cantFindCourt)).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(amendCourtService.validateCourtFields(Mockito.any(),Mockito.any())).thenReturn(Boolean.TRUE);
        CallbackResponse response =  callbackController
            .validateCourtFields(callbackRequest);
        Assertions.assertNotNull(response.getErrors());
    }

    @Test
    public void testCopyFL401CasenameToC100CaseNameForChildDetailsRevised() throws Exception {

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("applicantOrRespondentCaseName", "test");
        caseDetails.put("caseTypeOfApplication", "C100");
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
            .orgPolicyReference("jfljsd")
            .orgPolicyCaseAssignedRole("APPLICANTSOLICITOR").build();
        caseDetails.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        Organisations org = Organisations.builder().name("testOrg").organisationIdentifier("abcd").build();
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CaseData caseData = CaseData.builder().id(123L).applicantCaseName("abcd").taskListVersion(TASK_LIST_VERSION_V2)
            .applicantOrganisationPolicy(applicantOrganisationPolicy).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseDetails).build()).build();
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken,s2sToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void fetchRoleAssignmentUserDoesntHaveRightRoles() {
        when(roleAssignmentService.validateIfUserHasRightRoles(authToken, CallbackRequest.builder().build())).thenReturn(false);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .fetchRoleAssignmentForUser(authToken, CallbackRequest.builder().build());
        assertEquals("The selected user does not have right roles to assign this case",
            aboutToStartOrSubmitCallbackResponse.getErrors().get(0));
    }

    @Test
    public void fetchRoleAssignmentUserHasRightRoles() {
        when(roleAssignmentService.validateIfUserHasRightRoles(authToken, CallbackRequest.builder().build())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .fetchRoleAssignmentForUser(authToken, CallbackRequest.builder().build());
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }

    @Test
    public void testAttachScanDocsWaChangeWithQuarantineDocsAndScannedDocs() {

        Element<QuarantineLegalDoc> courtStaffQuanElement = element(QuarantineLegalDoc.builder().build());
        Element<QuarantineLegalDoc> cafcassQuanElement = element(QuarantineLegalDoc.builder().build());
        Element<QuarantineLegalDoc> legalProfDocQuanElement = element(QuarantineLegalDoc.builder().build());

        List<Element<QuarantineLegalDoc>> courtStaffQuanList = Collections.singletonList(courtStaffQuanElement);
        List<Element<QuarantineLegalDoc>> cafcassQuanList = Collections.singletonList(cafcassQuanElement);
        List<Element<QuarantineLegalDoc>> legalProfDocQuanList = Collections.singletonList(legalProfDocQuanElement);

        Element<ScannedDocument> scannedDocElement = element(ScannedDocument.builder().build());
        List<Element<ScannedDocument>> scannedDocList = Collections.singletonList(scannedDocElement);

        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtStaffQuarantineDocsList(courtStaffQuanList)
                                           .cafcassQuarantineDocsList(cafcassQuanList)
                                           .legalProfQuarantineDocsList(legalProfDocQuanList)
                                           .build())
            .scannedDocuments(scannedDocList)
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        doCallRealMethod().when(manageDocumentsService).setFlagsForWaTask(any(),any(),any(),any());

        AboutToStartOrSubmitCallbackResponse response = callbackController
            .attachScanDocsWaChange(authToken, s2sToken, callbackRequest);
        assertNotNull(response.getData().get(MANAGE_DOCUMENTS_RESTRICTED_FLAG));
        assertEquals(TRUE, response.getData().get(MANAGE_DOCUMENTS_RESTRICTED_FLAG));
        assertNull(response.getData().get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    public void testAttachScanDocsWaChangeWithoutQuarantineDocsAndScannedDocs() {

        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .build())
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        doCallRealMethod().when(manageDocumentsService).setFlagsForWaTask(any(),any(),any(),any());

        AboutToStartOrSubmitCallbackResponse response = callbackController
            .attachScanDocsWaChange(authToken, s2sToken, callbackRequest);
        assertNotNull(response.getData().get(MANAGE_DOCUMENTS_RESTRICTED_FLAG));
        assertNotNull(response.getData().get(MANAGE_DOCUMENTS_TRIGGERED_BY));
        assertEquals(TRUE, response.getData().get(MANAGE_DOCUMENTS_RESTRICTED_FLAG));
        assertEquals(STAFF, response.getData().get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    public void testAttachScanDocsWaChangeInvalidClientException() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(stringObjectMap).build()).build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);

        doCallRealMethod().when(manageDocumentsService).setFlagsForWaTask(any(),any(),any(),any());

        assertExpectedException(() -> {
            callbackController.attachScanDocsWaChange(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

}
