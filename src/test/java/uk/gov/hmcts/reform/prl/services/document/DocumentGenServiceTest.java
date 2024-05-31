package uk.gov.hmcts.reform.prl.services.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.InvalidResourceException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.services.DeleteDocumentService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_LIST_ON_NOTICE_FL404B_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C1A_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C8_BLANK_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_DRAFT_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_DRAFT_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_PRIVACY_NOTICE_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_APPLICATION_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENG_DOC_GEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PREVIOUS_ORDERS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUCCESS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TENANCY_MORTGAGE_AGREEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@SuppressWarnings({"java:S1607"})
public class DocumentGenServiceTest {

    @Mock
    DgsService dgsService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    OrganisationService organisationService;

    @Mock
    DeleteDocumentService deleteDocumentService;

    @InjectMocks
    DocumentGenService documentGenService;

    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    DgsApiClient dgsApiClient;

    @Mock
    IdamClient idamClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    UploadDocumentService uploadService;

    @Mock
    private Time dateTime;

    @Mock
    C100DocumentTemplateFinderService c100DocumentTemplateFinderService;

    @Mock
    AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Value("${document.templates.fl401.fl401_resp_c8_template_welsh}")
    protected String fl401RespC8TemplateWelsh;

    public static final String authToken = "Bearer TestAuthToken";


    CaseData c100CaseData;
    CaseData c100CaseDataFinal;
    CaseData c100CaseDataC1A;

    CaseData fl401CaseData;
    CaseData fl401CaseData1;
    CaseData c100CaseDataNotIssued;
    PartyDetails partyDetails;
    AllegationOfHarm allegationOfHarmYes;
    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;
    MockMultipartFile file;

    private DocumentRequest documentRequest;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseService caseService;

    @Mock
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private UserService userService;

    @Mock
    private ManageDocumentsService manageDocumentsService;

    private Document caseDoc;
    private QuarantineLegalDoc quarantineCaseDoc;


    @Before
    public void setUp() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
                                                                                        .addressLine1("29, SEATON DRIVE")
                                                                                        .addressLine2("test line")
                                                                                        .townCity("NORTHAMPTON")
                                                                                        .postCode("NN3 9SS")
                                                                                        .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();


        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();

        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);

        ApplicantConfidentialityDetails applicantConfidentialityDetails = ApplicantConfidentialityDetails.builder()
            .phoneNumber("1234567890")
            .firstName("UserFirst")
            .lastName("UserLast")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .email("test@confidential.com")
            .build();

        Element<ApplicantConfidentialityDetails> applicantConfidential = Element.<ApplicantConfidentialityDetails>builder()
            .value(applicantConfidentialityDetails).build();
        List<Element<ApplicantConfidentialityDetails>> applicantConfidentialList = Collections.singletonList(
            applicantConfidential);

        OtherPersonConfidentialityDetails otherPersonConfidentialityDetails = uk.gov.hmcts.reform.prl.models.complextypes.confidentiality
            .OtherPersonConfidentialityDetails.builder()
            .isPersonIdentityConfidential(Yes)
            .firstName("test1")
            .lastName("last1")
            .relationshipToChildDetails("uncle")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .build();

        Element<OtherPersonConfidentialityDetails> otherPerson = Element.<OtherPersonConfidentialityDetails>builder()
            .value(otherPersonConfidentialityDetails).build();
        List<Element<OtherPersonConfidentialityDetails>> otherPersonList = Collections.singletonList(otherPerson);


        ChildConfidentialityDetails childConfidentialityDetails = ChildConfidentialityDetails.builder()
            .firstName("ChildFirst")
            .lastName("ChildLast")
            .otherPerson(otherPersonList)
            .build();

        Element<ChildConfidentialityDetails> childConfidential = Element.<ChildConfidentialityDetails>builder()
            .value(childConfidentialityDetails).build();
        List<Element<ChildConfidentialityDetails>> childConfidentialList = Collections.singletonList(childConfidential);

        allegationOfHarmYes = AllegationOfHarm.builder()
            .allegationsOfHarmYesNo(Yes).build();

        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(Yes).build())
            .taskListVersion(TASK_LIST_VERSION_V2)
            .applicants(listOfApplicants)
            .state(State.CASE_ISSUED)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        c100CaseDataNotIssued = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .applicants(listOfApplicants)
            .state(State.CASE_WITHDRAWN)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        c100CaseDataFinal = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(No).build())
            .taskListVersion(TASK_LIST_VERSION_V2)
            //.allegationsOfHarmYesNo(Yes)
            .applicants(listOfApplicants)
            .state(State.CASE_ISSUED)
            //.allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        c100CaseDataC1A = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(listOfApplicants)
            .state(State.CASE_ISSUED)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .taskListVersion(TASK_LIST_VERSION_V2)
            //.allegationsOfHarmYesNo(Yes)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        fl401CaseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsWithOrganisations)
            .respondentsFL401(partyDetailsWithOrganisations)
            .isEngDocGen("Yes")
            .isWelshDocGen("Yes")
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .home(homefull)
            .build();

        fl401CaseData1 = CaseData.builder()
            .id(1234567L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .isEngDocGen("Yes")
            .isWelshDocGen("Yes")
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .home(homefull)
            .build();

        file
            = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );

        ReflectionTestUtils.setField(documentGenService, "organisationService", organisationService);
        ReflectionTestUtils.setField(documentGenService, "documentLanguageService", documentLanguageService);
        ReflectionTestUtils.setField(documentGenService, "dgsService", dgsService);
        ReflectionTestUtils.setField(
            documentGenService,
            "c100DocumentTemplateFinderService",
            c100DocumentTemplateFinderService
        );
        ReflectionTestUtils.setField(
            documentGenService,
            "allegationOfHarmRevisedService",
            allegationOfHarmRevisedService
        );
        ReflectionTestUtils.setField(documentGenService, "caseDocumentClient", caseDocumentClient);
        ReflectionTestUtils.setField(documentGenService, "uploadService", uploadService);
        ReflectionTestUtils.setField(documentGenService, "dgsApiClient", dgsApiClient);
        ReflectionTestUtils.setField(manageDocumentsService, "objectMapper", objectMapper);

        doCallRealMethod().when(manageDocumentsService).moveDocumentsToQuarantineTab(any(), any(), any(), any());
        doCallRealMethod().when(manageDocumentsService).moveDocumentsToRespectiveCategoriesNew(
            any(),
            any(),
            any(),
            any(),
            any()
        );
        doCallRealMethod().when(manageDocumentsService).getRestrictedOrConfidentialKey(any());
        doCallRealMethod().when(manageDocumentsService).getQuarantineDocumentForUploader(any(), any());
        doCallRealMethod().when(manageDocumentsService).moveToConfidentialOrRestricted(any(), any(), any(), any());

        documentRequest = DocumentRequest.builder()
            .caseId("123")
            .categoryId("POSITION_STATEMENTS")
            .partyId("00000000-0000-0000-0000-000000000000")
            .partyName("appf appl")
            .partyType("applicant")
            .restrictDocumentDetails("test details")
            .freeTextStatements("free text to generate document")
            .build();

        caseDoc = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .documentBinaryUrl(null)
            .documentHash(null)
            .categoryId(null)
            .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant()))
            .build();

        quarantineCaseDoc = QuarantineLegalDoc.builder()
            .categoryId("positionStatements")
            .positionStatementsDocument(caseDoc)
            .build();
    }

    @Test
    public void generateDocsForC100Test() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        c100CaseData = c100CaseData.toBuilder().allegationOfHarmRevised(AllegationOfHarmRevised
                                                                            .builder().newAllegationsOfHarmYesNo(Yes).build()).allegationOfHarm(
            null).build();
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseData);
        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForC100TestFinalDoc() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(c100CaseDataFinal);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(c100CaseDataFinal);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataFinal);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseDataFinal);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        verifyNoMoreInteractions(dgsService);
    }


    @Test
    public void generateDocsForFL401TestWithOrganisation() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }


    @Test
    public void testGenerateDraftDocumentEng() throws Exception {
        CaseData caseData = CaseData.builder().build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        Map<String, Object> docMap = documentGenService.generateDraftDocuments(authToken, caseData);
        assertTrue(docMap.containsKey(DRAFT_APPLICATION_DOCUMENT_FIELD));

    }

    @Test
    public void testGenerateDraftDocumentWelsh() throws Exception {
        CaseData caseData = CaseData.builder().build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        Map<String, Object> docMap = documentGenService.generateDraftDocuments(authToken, caseData);
        assertTrue(docMap.containsKey(DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD));

    }

    @Test
    public void generateDraftDocsForC100Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseData);
        Map<String, Object> stringObjectMap = documentGenService.generateDraftDocuments(authToken, c100CaseData);

        assertTrue(stringObjectMap.containsKey(DRAFT_APPLICATION_DOCUMENT_FIELD));
        assertTrue(stringObjectMap.containsKey(DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD));

        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(1)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForC100TestWithC1A() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);
        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseDataC1A);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForFL401Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForFL401TestWithChildConfidentialInfo() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData1);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }


    @Test
    public void testC8Formgenerationbasedconconfidentiality2() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(homefull)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }


    @Test
    public void testC8Formgenerationbasedconconfidentiality_withoutTypeofOrders() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        orders = TypeOfApplicationOrders.builder()
            .orderType(null)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(null)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(homefull)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testC8FormGenerationBasedcOnConfidentiality_() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testDocsNullValueWhenAbsent() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(1)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testDocsNullValueWhenEnglishNotWesh() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testDocsNullValueWhenWelshNotenglish() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocuments(authToken, fl401CaseData);
        verify(dgsService, times(1)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testSingleDocGenerationWithMap() throws Exception {
        Map<String, Object> respondentDetails = new HashMap<>();
        documentGenService.generateSingleDocument(
            "auth",
            c100CaseData,
            DOCUMENT_COVER_SHEET_HINT,
            false,
            respondentDetails
        );
        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void testDocGenerationWithNoName() {
        documentGenService.convertToPdf("auth", Document.builder().build());
        verify(caseDocumentClient, times(0)).getDocumentBinary(
            authToken, "s2s token", generatedDocumentInfo.getUrl()
        );
    }

    @Test
    public void testDocGenerationWithNoPeriods() {
        documentGenService.convertToPdf("auth", Document.builder().documentFileName("i").build());
        verify(caseDocumentClient, times(0)).getDocumentBinary(
            authToken, "s2s token", generatedDocumentInfo.getUrl()
        );
    }

    @Test
    public void testSingleDocGeneration() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, DOCUMENT_COVER_SHEET_HINT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testSingleDocGenerationC1A() throws Exception {
        c100CaseData = c100CaseData.toBuilder().taskListVersion(TASK_LIST_VERSION_V2).build();
        documentGenService.generateSingleDocument("auth", c100CaseData, C1A_HINT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }


    @Test
    public void testBlankDocsGeneration() throws Exception {
        CaseData emptyCaseData = CaseData.builder().build();
        documentGenService.generateSingleDocument("auth", emptyCaseData, DOCUMENT_C7_DRAFT_HINT, false);
        documentGenService.generateSingleDocument("auth", emptyCaseData, DOCUMENT_C8_BLANK_HINT, false);
        documentGenService.generateSingleDocument("auth", emptyCaseData, DOCUMENT_C1A_BLANK_HINT, false);
        documentGenService.generateSingleDocument("auth", emptyCaseData, DOCUMENT_PRIVACY_NOTICE_HINT, false);
        documentGenService.generateSingleDocument("auth", emptyCaseData, CITIZEN_HINT, false);

        verify(dgsService, times(5)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testDeleteDocument() {
        //Given
        DocumentResponse documentResponse = DocumentResponse
            .builder()
            .status("Success")
            .build();
        doNothing().when(uploadService).deleteDocument(authToken, "TEST_DOCUMENT_ID");
        //When
        DocumentResponse response = documentGenService.deleteDocument(authToken, "TEST_DOCUMENT_ID");
        //Then
        assertEquals(documentResponse, response);
    }

    @Test
    public void testDeleteDocumentException() {
        //Given
        DocumentResponse documentResponse = DocumentResponse
            .builder()
            .status("Success")
            .build();
        doThrow(new RuntimeException("Exception while delete document")).when(uploadService).deleteDocument(any(),
                                                                                                            any());

        assertExpectedException(() -> {
            documentGenService
                .deleteDocument(authToken, "TEST_DOCUMENT_ID");
        }, RuntimeException.class, "Exception while delete document");

    }

    @Test
    public void testDownloadDocument() {
        //Given
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);

        when(uploadService.downloadDocument(authToken, "TEST_DOCUMENT_ID"
        )).thenReturn(expectedResponse);

        //When
        ResponseEntity<?> response = documentGenService.downloadDocument(authToken, "TEST_DOCUMENT_ID");
        //Then
        assertEquals(OK, response.getStatusCode());
    }

    @Test(expected = RuntimeException.class)
    public void testDownloadDocumentThrowException() {
        when(uploadService.downloadDocument(authToken, "TEST_DOCUMENT_ID"
        )).thenThrow(RuntimeException.class);
        documentGenService.downloadDocument(authToken, "TEST_DOCUMENT_ID");
    }

    @Test
    public void testGenerateDocumentsForCitizenSubmissionForEnglish() throws Exception {

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );

        Map<String, Object> updatedCaseData = documentGenService.generateDocumentsForCitizenSubmission(
            authToken,
            c100CaseData
        );
        assertEquals(updatedCaseData.get(IS_ENG_DOC_GEN), Yes.toString());
        assertTrue(updatedCaseData.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
    }

    @Test
    public void testGenerateDocumentsForCitizenSubmissionForWelsh() throws Exception {

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );

        Map<String, Object> updatedCaseData = documentGenService.generateDocumentsForCitizenSubmission(
            authToken,
            c100CaseData
        );
        assertEquals(updatedCaseData.get(IS_ENG_DOC_GEN), Yes.toString());
        assertTrue(!updatedCaseData.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
    }

    @Test
    public void testGenerateDocumentsForCitizen() throws Exception {
        //Given
        when(documentLanguageService.docGenerateLang(c100CaseData)).thenReturn(DocumentLanguage
                                                                                   .builder().isGenEng(true).build());
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        //When
        Map<String, Object> response = documentGenService.generateDocumentsForCitizenSubmission(
            authToken,
            c100CaseData
        );
        //Then
        assertEquals(Yes.toString(), response.get(IS_ENG_DOC_GEN));
    }

    @Test
    public void testSingleDocGenerationForEnglish() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, C7_FINAL_RESPONDENT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testSingleDocGenerationForWelsh() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, C7_FINAL_WELSH, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testSingleDocGenerationDefault() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, "", false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testSingleDocGenerationC8DraftHint() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, C8_DRAFT_HINT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testSingleDocGenerationC1ADraftHint() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, C1A_DRAFT_HINT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testGenerateC8DocumentForRespondent() throws Exception {
        Map<String, Object> respondentDetails = new HashMap<>();
        respondentDetails.put("dynamic_fileName", "test.pdf");
        documentGenService.generateSingleDocument(
            "auth",
            c100CaseData,
            DOCUMENT_COVER_SHEET_HINT,
            false,
            respondentDetails
        );
        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void generateDocsForC100CaseNotIssued() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataNotIssued);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataNotIssued);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataNotIssued);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseDataNotIssued);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_DRAFT_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_DRAFT_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C1A));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testGenerateCitizenDocument() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", "Your position statements");
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithYourWitnessStatement() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", YOUR_WITNESS_STATEMENTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithOtherWitnessStatement() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", OTHER_WITNESS_STATEMENTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithMedicalRecords() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", MEDICAL_RECORDS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithMail_screenshots() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", MAIL_SCREENSHOTS_MEDIA_FILES);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithLettersFromSchools() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", LETTERS_FROM_SCHOOL);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithTenancyMortgageAgreement() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", TENANCY_MORTGAGE_AGREEMENTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithPreviousOrderSubmitted() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", PREVIOUS_ORDERS_SUBMITTED);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithMedicalReports() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", MEDICAL_REPORTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithPaternityTestReports() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", PATERNITY_TEST_REPORTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithDrugAndAlcoholReports() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", DRUG_AND_ALCOHOL_TESTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithPoliceReports() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", POLICE_REPORTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateCitizenDocumentWithOtherDocument() throws Exception {
        Map<String, String> documentValues = new HashMap<>();
        documentValues.put("caseId", "1664294549087405");
        documentValues.put("freeTextUploadStatements", "testing document gen");
        documentValues.put("parentDocumentType", "Witness statements and evidence");
        documentValues.put("documentType", OTHER_DOCUMENTS);
        documentValues.put("partyName", "Sonali Citizen");
        documentValues.put("partyId", "0c09b130-2eba-4ca8-a910-1f001bac01e6");
        documentValues.put("documentRequestedByCourt", "No");
        documentValues.put("isApplicant", "Yes");

        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(documentValues)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );

        documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 1);
        verify(dgsService, times(1)).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(GenerateAndUploadDocumentRequest.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateC7Document() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationOfHarm(allegationOfHarmYes)
            .state(State.CASE_ISSUED)
            .build();

        DocumentLanguage docLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(docLanguage);
        Map<String, Object> responseMap = documentGenService.generateC7DraftDocuments(authToken, caseData);
        assertNotNull(responseMap);
    }

    @Test
    public void testSingleDocGenerationForSolicitorC7() throws Exception {
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C7_DRAFT_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C7_FINAL_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C1A_DRAFT_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C1A_FINAL_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, SOLICITOR_C1A_WELSH_FINAL_DOCUMENT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, C8_RESP_DRAFT_HINT, false);
        documentGenService.generateSingleDocument("auth", c100CaseData, C8_RESP_FINAL_HINT, false);
        verify(dgsService, times(8)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void generateDocumentsForTestingSupportForC100Test() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            c100CaseData
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(6)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(6)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocumentsForTestingSupportForC100TestFinalDoc() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        when(organisationService.getApplicantOrganisationDetails(any(CaseData.class))).thenReturn(c100CaseDataFinal);
        when(organisationService.getRespondentOrganisationDetails(any(CaseData.class))).thenReturn(c100CaseDataFinal);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            c100CaseDataFinal
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(4)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        verify(dgsService, times(4)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocumentsForTestingSupportForFL401TestWithOrganisation() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            fl401CaseData
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocumentsForTestingSupportForC100TestWithC1A() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            c100CaseDataC1A
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(6)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(6)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocumentsForTestingSupportForFL401Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            fl401CaseData
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocumentsForTestingSupportForFL401TestWithChildConfidentialInfo() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocumentsForTestingSupport(
            authToken,
            fl401CaseData1
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testGenerateDocumentsForTestingSupportC8Formgenerationbasedconconfidentiality2() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(homefull)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateDocumentsForTestingSupportC8Formgenerationbasedconconfidentiality_withoutTypeofOrders() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        orders = TypeOfApplicationOrders.builder()
            .orderType(null)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(null)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(homefull)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testGenerateDocumentsForTestingSupportC8FormGenerationBasedcOnConfidentiality_() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testGenerateDocumentsForTestingSupportDocsNullValueWhenAbsent() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testGenerateDocumentsForTestingSupportDocsNullValueWhenEnglishNotWesh() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testGenerateDocumentsForTestingSupportDocsNullValueWhenWelshNotenglish() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .canYouProvidePhoneNumber(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .home(null)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetailsForFL401(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        documentGenService.generateDocumentsForTestingSupport(authToken, fl401CaseData);
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void testSingleDocGenerationForFl404b() throws Exception {
        documentGenService.generateSingleDocument("auth", fl401CaseData, DA_LIST_ON_NOTICE_FL404B_DOCUMENT, false);
        verify(dgsService, times(1)).generateDocument(Mockito.anyString(), any(CaseDetails.class), Mockito.any());
    }

    @Test
    public void testForGetDocumentBytes() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);

        when(caseDocumentClient.getDocumentBinary(authToken, "s2s token", generatedDocumentInfo.getUrl()))
            .thenReturn(expectedResponse);

        documentGenService.getDocumentBytes(generatedDocumentInfo.getUrl(), authToken, "s2s token");
        verify(caseDocumentClient, times(1)).getDocumentBinary(
            authToken, "s2s token", generatedDocumentInfo.getUrl()
        );
    }

    @Test
    public void testForGetDocumentBytesException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        assertExpectedException(() -> {
            documentGenService
                .getDocumentBytes(generatedDocumentInfo.getUrl(), authToken, "s2s token");
        }, InvalidResourceException.class, "Resource is invalid TestUrl");

    }

    @Test
    public void testForGetDocumentBytesFileNotFoundException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Resource expectedResource = new ClassPathResource("documents/document1.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(caseDocumentClient.getDocumentBinary(authToken, "s2s token", generatedDocumentInfo.getUrl()))
            .thenReturn(expectedResponse);

        assertExpectedException(() -> {
            documentGenService
                .getDocumentBytes(generatedDocumentInfo.getUrl(), authToken, "s2s token");
        }, InvalidResourceException.class, "Doc name TestUrl");

    }

    @Test
    public void testForConvertToPdf() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("TestUrl")
            .hashToken("testHashToken")
            .build();

        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(caseDocumentClient.getDocumentBinary(authToken, "s2s token", generatedDocumentInfo.getUrl()))
            .thenReturn(expectedResponse);

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("FL401-Final.docx")
            .build();

        when(dgsApiClient.convertDocToPdf(anyString(), anyString(), any()))
            .thenReturn(generatedDocumentInfo);

        documentGenService.convertToPdf(authToken, document);

        verify(caseDocumentClient, times(1)).getDocumentBinary(
            authToken, "s2s token", generatedDocumentInfo.getUrl()
        );
    }

    @Test
    public void testForConvertToPdfException() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("TestUrl")
            .hashToken("testHashToken")
            .build();

        Resource expectedResource = new ClassPathResource("documents/document1.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(caseDocumentClient.getDocumentBinary(authToken, "s2s token", generatedDocumentInfo.getUrl()))
            .thenReturn(expectedResponse);

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("FL401-Final.docx")
            .build();

        when(dgsApiClient.convertDocToPdf(anyString(), anyString(), any()))
            .thenReturn(generatedDocumentInfo);

        assertExpectedException(() -> {
            documentGenService
                .convertToPdf(authToken, document);
        }, InvalidResourceException.class, "Doc name FL401-Final.docx");
    }


    @Test
    public void testUploadDocument() throws Exception {

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = randomAlphanumeric(10);
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = randomAlphanumeric(10);

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphanumeric(10);

        when(uploadService.uploadDocument(any(), any(), any(), any())).thenReturn(document);

        documentGenService.uploadDocument(authToken, file);

        verify(uploadService, times(1)).uploadDocument(
            file.getBytes(),
            file.getOriginalFilename(),
            file.getContentType(),
            authToken
        );

        verifyNoMoreInteractions(uploadService);

    }


    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testGenerateAndUploadDocument() throws Exception {
        //Given
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        //When
        doReturn(generatedDocumentInfo).when(dgsService).generateCitizenDocument(
            Mockito.anyString(),
            Mockito.any(DocumentRequest.class),
            Mockito.any()
        );
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        //Action
        DocumentResponse documentResponse = documentGenService.generateAndUploadDocument(authToken, documentRequest);

        //Then
        assertNotNull(documentResponse);
        assertNotNull(documentResponse.getDocument());
        assertEquals(SUCCESS, documentResponse.getStatus());
    }

    @Test
    public void testCitizenUploadDocumentsAndMoveToQuarantine() throws Exception {
        //Given
        documentRequest = documentRequest.toBuilder()
            .isConfidential(Yes)
            .isRestricted(Yes)
            .restrictDocumentDetails("test")
            .documents(List.of(Document.builder().build())).build();

        CaseData caseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .reviewDocuments(ReviewDocuments.builder().build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .build();

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(new ObjectMapper()))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContents = new StartAllTabsUpdateDataContent(authToken,
                                                                                                         EventRequestData.builder().build(),
                                                                                                         StartEventResponse.builder().build(),
                                                                                                         stringObjectMap,
                                                                                                         caseData,
                                                                                                         null
        );
        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.CITIZEN_CASE_UPDATE.getValue())).thenReturn(
            startAllTabsUpdateDataContents);


        when(caseService.getCase(any(), any())).thenReturn(caseDetails);


        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when((userService.getUserDetails(any()))).thenReturn(UserDetails.builder()
                                                                 .roles(List.of(Roles.CITIZEN.getValue())).build());

        //Action
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsUpdated = documentGenService.citizenSubmitDocuments(
            authToken,
            documentRequest
        );

        //Then
        assertNotNull(caseDetails);
        //CORRECT ASSERTIONS LATER

        //assertNotNull(caseDetailsUpdated);
        //assertNotNull(caseDetailsUpdated.getData());

        //CaseData caseUpdated = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        // assertNotNull(caseUpdated.getDocumentManagementDetails().getCitizenQuarantineDocsList());
    }

    @Test
    public void testCitizenUploadDocumentsAndMoveRespectiveCategory() throws Exception {
        //Given
        documentRequest = documentRequest.toBuilder()
            .categoryId("FM5_STATEMENTS")
            .isConfidential(No)
            .isRestricted(No)
            .restrictDocumentDetails("test")
            .documents(List.of(caseDoc)).build();
        CaseData caseData = CaseData.builder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .reviewDocuments(ReviewDocuments.builder().build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(new ObjectMapper()))
            .build();

        //When
        when(caseService.getCase(any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fm5StatementsDocument", caseDoc);
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc
            .builder()
            .hasTheConfidentialDocumentBeenRenamed(
                YesOrNo.No)
            .isConfidential(null)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                          .documentUrl("00000000-0000-0000-0000-000000000000")
                          .documentFileName("test")
                          .build())
            .isRestricted(null)
            .restrictedDetails(null)
            .categoryId("test")
            .uploaderRole("Citizen")
            .build();
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(QuarantineLegalDoc.class))).thenReturn(
            quarantineLegalDoc);
        when((userService.getUserDetails(any()))).thenReturn(UserDetails.builder()
                                                                 .roles(List.of(Roles.CITIZEN.getValue())).build());

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContents = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap,
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.CITIZEN_CASE_UPDATE.getValue())).thenReturn(
            startAllTabsUpdateDataContents);


        //Action
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsUpdated = documentGenService.citizenSubmitDocuments(
            authToken,
            documentRequest
        );

        //Then
        assertNotNull(caseDetails);
        //CORRECT ASSERTIONS LATER
        //assertNotNull(caseDetailsUpdated);
        //assertNotNull(caseDetailsUpdated.getData());

    }

    @Test
    public void testCitizenCoverLetterTemplateEnglish() {
        ReflectionTestUtils.setField(documentGenService, "docCoverSheetServeOrderTemplate", "citizen_cover_letter_en");
        String template = documentGenService.getTemplate(c100CaseData, DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT, false);

        assertNotNull(template);
        assertEquals("citizen_cover_letter_en", template);
    }

    @Test
    public void testCitizenCoverLetterTemplateWelsh() {
        ReflectionTestUtils.setField(
            documentGenService,
            "docCoverSheetWelshServeOrderTemplate",
            "citizen_cover_letter_wel"
        );
        String template = documentGenService.getTemplate(c100CaseData, DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT, true);

        assertNotNull(template);
        assertEquals("citizen_cover_letter_wel", template);
    }

    @Test
    public void testGenerateDraftDocumentsForCaseResubmissionTest1() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        c100CaseData = c100CaseData.toBuilder().allegationOfHarmRevised(AllegationOfHarmRevised
                                                                            .builder()
                                                                            .newAllegationsOfHarmYesNo(Yes).build())
            .allegationOfHarm(null).build();
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseData);
        Map<String, Object> stringObjectMap = documentGenService.generateDraftDocumentsForC100CaseResubmission(
            authToken,
            c100CaseData
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_DRAFT_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_DRAFT_WELSH));
    }

    @Test
    public void testGenerateDraftDocumentsForCaseResubmissionTest2() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );

        c100CaseData = c100CaseData.toBuilder()
            .applicantsConfidentialDetails(new ArrayList<>())
            .childrenConfidentialDetails(new ArrayList<>())
            .allegationOfHarmRevised(AllegationOfHarmRevised
                                         .builder().newAllegationsOfHarmYesNo(No).build()).allegationOfHarm(null).build();
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(allegationOfHarmRevisedService.updateChildAbusesForDocmosis(Mockito.any(CaseData.class))).thenReturn(
            c100CaseData);
        Map<String, Object> stringObjectMap = documentGenService.generateDraftDocumentsForC100CaseResubmission(
            authToken,
            c100CaseData
        );

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_DRAFT_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_DRAFT_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_DRAFT_WELSH));
    }
}



