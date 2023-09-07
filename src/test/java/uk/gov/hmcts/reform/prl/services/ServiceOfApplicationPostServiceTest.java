package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FILE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@Ignore
public class ServiceOfApplicationPostServiceTest {

    @InjectMocks
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Value("${citizen.url}")
    private String citizenUrl;
    public static final String s2sToken = "s2s token";
    private static final String AUTH = "Auth";

    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "Abc123EFGH";
    private static final String LETTER_TYPE = "ApplicationPack";
    private static final String CONTENT_TYPE = "application/json";
    private DynamicMultiSelectList dynamicMultiSelectList;

    @Before
    public void setup() {
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().label("standardDirectionsOrder").build())).build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

    @Test
    public void testSendViaPostToOtherPeopleInCase() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Document> packN = List.of(Document.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaOtherParties(DynamicMultiSelectList.builder()
                                                           .value(List.of(dynamicListElement))
                                                           .build()).build())
            .othersToNotify(otherParities)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);
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
        BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder()
            .recipientsName("fn ln")
            .postalAddress(Address.builder()
                               .addressLine1("line1")
                               .build())
            .servedParty(SERVED_PARTY_OTHER)
            .timeStamp(currentDate)
            .printDocs(documentList.stream().map(e -> element(e)).collect(Collectors.toList()))
            .build();
        assertNotNull(serviceOfApplicationPostService
                         .sendPostNotificationToParty(caseData,
                                                      AUTH, partyDetails, documentList, SERVED_PARTY_OTHER));

    }

    @Test
    public void testGetCoverLetterGeneratedDocInfo() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Document> packN = List.of(Document.builder().build());

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaOtherParties(DynamicMultiSelectList.builder()
                                                           .value(List.of(dynamicListElement))
                                                           .build())
                                      .coverPageAddress(Address.builder().addressLine1("157").addressLine2("London")
                                                            .postCode("SE1 234").country("UK").build())
                                      .build())
            .othersToNotify(otherParities)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);
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

        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        assertNotNull(serviceOfApplicationPostService
                          .getCoverLetterGeneratedDocInfo(caseData,
                                                       AUTH, address, "test name"));

    }

    @Test
    public void testGetCoverLetterGeneratedDocInfoWithWelsh() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Document> packN = List.of(Document.builder().build());

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaOtherParties(DynamicMultiSelectList.builder()
                                                           .value(List.of(dynamicListElement))
                                                           .build())
                                      .coverPageAddress(Address.builder().addressLine1("157").addressLine2("London")
                                                            .postCode("SE1 234").country("UK").build())
                                      .build())
            .othersToNotify(otherParities)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);
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

        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        assertNotNull(serviceOfApplicationPostService
                          .getCoverLetterGeneratedDocInfo(caseData,
                                                          AUTH, address, "test name"));

    }

    @Test
    public void testStaticDocsForC100Applicant() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
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
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_APPLICANT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();

        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile("files", FILE_NAME, CONTENT_TYPE, pdf);
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();

        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(caseDocumentClient.uploadDocuments(AUTH, s2sToken, CASE_TYPE, JURISDICTION, newArrayList(file))).thenReturn(uploadResponse);

        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        assertNotNull(serviceOfApplicationPostService.getStaticDocs(AUTH, caseData));


    }

    @Test
    public void testStaticDocsForFL401() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
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
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicant)
            .respondentsFL401(PartyDetails.builder()
                                 .solicitorEmail("test@gmail.com")
                                 .representativeLastName("LastName")
                                 .representativeFirstName("FirstName")
                                 .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                 .build())
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");



        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_APPLICANT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();

        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile("files", FILE_NAME, CONTENT_TYPE, pdf);
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();

        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(caseDocumentClient.uploadDocuments(AUTH, s2sToken, CASE_TYPE, JURISDICTION, newArrayList(file))).thenReturn(uploadResponse);

        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        assertNotNull(serviceOfApplicationPostService.getStaticDocs(AUTH, caseData));


    }

    public static uk.gov.hmcts.reform.ccd.document.am.model.Document testDocument() {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = randomAlphaNumeric;
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = randomAlphaNumeric;

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphaNumeric;

        return document;
    }
}
