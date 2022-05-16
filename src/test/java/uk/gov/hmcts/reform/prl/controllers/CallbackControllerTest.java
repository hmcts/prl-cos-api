package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


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
    private WorkflowResult workflowResult;

    @Mock
    private DgsService dgsService;

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
    SendgridService sendgridService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    C100JsonMapper c100JsonMapper;

    @Mock
    private CaseEventService caseEventService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private AddCaseNoteService addCaseNoteService;
  
    @Mock
    private DocumentGenService documentGenService;

    public static final String authToken = "Bearer TestAuthToken";

    private static final Map<String, Object> c100DraftMap = new HashMap<>();
    private static final Map<String, Object> c100DocsMap = new HashMap<>();

    private static final Map<String, Object> fl401DraftMap = new HashMap<>();
    private static final Map<String, Object> fl401DocsMap = new HashMap<>();

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
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
    }

    @Test
    public void testConfirmMiamApplicationOrExemption() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();


        when(applicationConsiderationTimetableValidationWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);

        callbackController.validateApplicationConsiderationTimetable(callbackRequest);

        verify(applicationConsiderationTimetableValidationWorkflow).run(callbackRequest);
        verifyNoMoreInteractions(applicationConsiderationTimetableValidationWorkflow);

    }

    @Test
    public void testvalidateApplicationConsiderationTimetable() throws WorkflowException {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();


        when(validateMiamApplicationOrExemptionWorkflow.run(callbackRequest))
            .thenReturn(workflowResult);

        callbackController.validateMiamApplicationOrExemption(callbackRequest);

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
            .welshLanguageRequirementApplication(LanguagePreference.english)
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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(c100DraftMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(authToken, callbackRequest);

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
            .welshLanguageRequirementApplication(LanguagePreference.english)
            .languageRequirementApplicationNeedWelsh(YesOrNo.Yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(fl401DraftMap);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(authToken, callbackRequest);

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
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(fl401DraftMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(authToken, callbackRequest);

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
        when(documentGenService.generateDraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(fl401DraftMap);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = callbackController.generateAndStoreDocument(authToken, callbackRequest);

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
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        doNothing().when(allTabsService).updateAllTabs(any(CaseData.class));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        callbackController.updateApplication(authToken, callbackRequest);

        verify(allTabsService, times(1)).updateAllTabs(any(CaseData.class));
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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verify(solicitorEmailService, times(1))
            .sendWithDrawEmailToSolicitorAfterIssuedState(callbackRequest.getCaseDetails(), userDetails);
        verify(caseWorkerEmailService, times(1))
            .sendWithdrawApplicationEmailToLocalCourt(callbackRequest.getCaseDetails(),"test@gmail.com");
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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verify(solicitorEmailService, times(timesCalled))
            .sendWithDrawEmailToSolicitor(callbackRequest.getCaseDetails(), userDetails);
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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verifyNoMoreInteractions(solicitorEmailService);
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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verifyNoMoreInteractions(solicitorEmailService);
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

        callbackController.sendEmailForSendToGatekeeper(authToken, callbackRequest);
        verify(caseWorkerEmailService, times(1))
            .sendEmailToGateKeeper(callbackRequest.getCaseDetails());
    }

    @Test
    public void resendNotificationtoRpaTest() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(1234L)
            .build();
        Map<String, Object> json = new HashMap<>();
        json.put("id", 1234L);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd
            .client.model.CallbackRequest
            .builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1234L)
                             .data(json)
                             .build())
            .build();
        when(objectMapper.convertValue(json, CaseData.class)).thenReturn(caseData);
        when(c100JsonMapper.map(caseData)).thenReturn(JsonValue.EMPTY_JSON_OBJECT);
        callbackController.resendNotificationtoRpa(authToken, callbackRequest);
        verify(sendgridService,times(1)).sendEmail(JsonValue.EMPTY_JSON_OBJECT);
    }

    @Test
    public void testCopyFL401CasenameToC100CaseName() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        Organisations org = Organisations.builder().name("testOrg").build();
        caseData.put("applicantOrRespondentCaseName", "test");
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, callbackRequest);
        assertEquals("test", aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void aboutToSubmitCaseCreationToC100ForNullCaseName() {

        Map<String, Object> caseData = new HashMap<>();
        Organisations org = Organisations.builder().name("testOrg").build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(organisationService.findUserOrganisation(Mockito.anyString()))
            .thenReturn(Optional.of(org));
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, callbackRequest);
        assertNull(aboutToStartOrSubmitCallbackResponse.getData().get("applicantCaseName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorName"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseSolicitorOrgName"));
    }

    @Test
    public void testAddCaseNumberSubmitted() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .addCaseNumberSubmitted(authToken, callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("issueDate"));
    }

    @Test
    public void aboutToSubmitCaseCreationToC100ForNullCaseNameWithException() {

        Map<String, Object> caseData = new HashMap<>();
        Organisations org = Organisations.builder().name("testOrg").build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(null);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .aboutToSubmitCaseCreation(authToken, callbackRequest);
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
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .copyManageDocsForTabs(authToken, callbackRequest);
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
            .furtherEvidences(List.of(Element.<FurtherEvidence>builder()
                                          .value(FurtherEvidence.builder()
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
                                                   .restrictCheckboxOtherDocuments(List.of(RestrictToCafcassHmcts.restrictToGroup))
                                                   .build())
                                        .build()))
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = callbackController
            .copyManageDocsForTabs(authToken, callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("mainAppDocForTabDisplay"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("correspondenceForTabDisplay"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("otherDocumentsForTabDisplay"));
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

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verify(solicitorEmailService, times(1))
            .sendWithDrawEmailToFl401SolicitorAfterIssuedState(callbackRequest.getCaseDetails(), userDetails);
        verify(caseWorkerEmailService, times(1))
            .sendWithdrawApplicationEmailToLocalCourt(callbackRequest.getCaseDetails(),"test@gmail.com");
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

        callbackController.sendEmailNotificationOnCaseWithdraw(authToken, callbackRequest);
        verify(solicitorEmailService, times(1))
            .sendWithDrawEmailToFl401Solicitor(callbackRequest.getCaseDetails(), userDetails);
        verifyNoMoreInteractions(caseWorkerEmailService);
    }
}


