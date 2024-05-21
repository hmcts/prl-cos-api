package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
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
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FILE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
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

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Value("${citizen.url}")
    private String citizenUrl;
    public static final String s2sToken = "s2s token";
    private static final String AUTH = "Auth";

    private static final String randomAlphaNumeric = "Abc123EFGH";
    private static final String CONTENT_TYPE = "application/json";
    private DynamicMultiSelectList dynamicMultiSelectList;

    @Before
    public void setup() throws Exception {
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().label("standardDirectionsOrder").build())).build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
    }

    @Test
    public void testSendViaPostToOtherPeopleInCase() {

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

        when(launchDarklyClient.isFeatureEnabled("soa-bulk-print")).thenReturn(true);

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

        assertNotNull(serviceOfApplicationPostService
                         .sendPostNotificationToParty(caseData,
                                                      AUTH, partyDetailsElement, documentList, SERVED_PARTY_OTHER));

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
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);

        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        assertNotNull(serviceOfApplicationPostService.getCoverSheets(caseData, AUTH, address, "test name", DOCUMENT_COVER_SHEET_HINT));
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
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);

        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        assertNotNull(serviceOfApplicationPostService.getCoverSheets(caseData, AUTH, address, "test name", DOCUMENT_COVER_SHEET_HINT));
    }

    @Test
    public void testStaticDocsForC100Applicant() {
        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();

        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(caseDocumentClient.uploadDocuments(Mockito.anyString(), Mockito.anyString(),
                                                Mockito.anyString(), Mockito.anyString(),
                                                Mockito.anyList())).thenReturn(uploadResponse);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(documentLanguageService.docGenerateLang(Mockito.any())).thenReturn(DocumentLanguage.builder()
                                                                                    .isGenWelsh(true)
                                                                                    .isGenEng(true).build());
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
        assertNotNull(serviceOfApplicationPostService.getStaticDocs(AUTH, "C100", caseData));
    }

    @Test
    public void testStaticDocsForFL401() {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

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

        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile("files", FILE_NAME, CONTENT_TYPE, pdf);
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();

        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(caseDocumentClient.uploadDocuments(AUTH, s2sToken, CASE_TYPE, JURISDICTION, newArrayList(file))).thenReturn(uploadResponse);

        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        assertNotNull(serviceOfApplicationPostService.getStaticDocs(AUTH, "FL401", caseData));


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

    @Test
    public void testGetCoverLetterForEnglish() throws Exception {

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();


        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(documentGenService.getTemplate(
            Mockito.any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Mockito.anyString());
        assertNotNull(serviceOfApplicationPostService.getCoverSheets(caseData, AUTH, address, "test name", DOCUMENT_COVER_SHEET_HINT));
    }

    @Test
    public void testGetCoverLetterForWelsh() throws Exception {

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();


        final Address address = Address.builder().addressLine1("157").addressLine2("London")
            .postCode("SE1 234").country("UK").build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(documentGenService.getTemplate(
            Mockito.any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Mockito.anyString());
        assertNotNull(serviceOfApplicationPostService.getCoverSheets(caseData, AUTH, address, "test name", DOCUMENT_COVER_SHEET_HINT));
    }

    @Test
    public void testGetCoverLetterWithNoAddressLine1() throws Exception {

        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();


        final Address address = Address.builder().build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
            .thenReturn(generatedDocumentInfo);
        when(documentGenService.getTemplate(
            Mockito.any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Mockito.anyString());
        assertTrue(serviceOfApplicationPostService.getCoverSheets(caseData, AUTH, address, "test name", DOCUMENT_COVER_SHEET_HINT).isEmpty());
    }

    @Test
    public void testRespondentCaseDataIsNotEmpty() {
        PartyDetails partyDetails = PartyDetails.builder()
            .lastName("Smith")
            .firstName("John")
                .build();
        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();
        CaseData caseDataResponse = serviceOfApplicationPostService.getRespondentCaseData(partyDetails, caseData);
        assertNotNull(caseDataResponse);
        assertEquals(12345,caseDataResponse.getId());
        assertNotNull(caseDataResponse.getRespondents());
        assertEquals("John", caseDataResponse.getRespondents().get(0).getValue().getFirstName());
        assertEquals("Smith", caseDataResponse.getRespondents().get(0).getValue().getLastName());
    }

    @Test
    public void testUploadedDocumentsServiceOfApplication() {
        final CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder()
                                                                 .documentUrl("documentUrl")
                                                                 .documentBinaryUrl("documentBinaryUrl")
                                                                 .documentHash("documentHash")
                                                                 .build())
                                                .specialArrangementsLetter(Document.builder()
                                                                               .documentUrl("documentUrl1")
                                                                               .documentBinaryUrl("documentBinaryUrl1")
                                                                               .documentHash("documentHash1")
                                                                               .build())
                                                .build())
            .build();
        List<GeneratedDocumentInfo> uploadedDocumentsList =
            serviceOfApplicationPostService.getUploadedDocumentsServiceOfApplication(caseData);
        assertNotNull(uploadedDocumentsList);
        assertEquals("documentUrl", uploadedDocumentsList.get(0).getUrl());
        assertEquals("documentBinaryUrl", uploadedDocumentsList.get(0).getBinaryUrl());
        assertEquals("documentHash", uploadedDocumentsList.get(0).getHashToken());
        assertEquals("documentUrl1", uploadedDocumentsList.get(1).getUrl());
        assertEquals("documentBinaryUrl1", uploadedDocumentsList.get(1).getBinaryUrl());
        assertEquals("documentHash1", uploadedDocumentsList.get(1).getHashToken());
    }

    @Test
    public void testFinalDocumentEnglish() {
        CaseData caseData =  CaseData.builder()
            .finalDocument(Document.builder()
                               .documentUrl("documentUrl")
                               .documentBinaryUrl("documentBinaryUrl")
                               .documentHash("documentHash")
                               .build())
            .build();
        Document finalDocument = serviceOfApplicationPostService.getFinalDocument(caseData);
        assertNotNull(finalDocument);
        assertEquals("documentUrl", finalDocument.getDocumentUrl());
        assertEquals("documentBinaryUrl", finalDocument.getDocumentBinaryUrl());
        assertEquals("documentHash", finalDocument.getDocumentHash());
    }

    @Test
    public void testFinalDocumentWelsh() {
        CaseData caseData =  CaseData.builder()
            .finalDocument(Document.builder()
                               .documentUrl("documentUrl")
                               .documentBinaryUrl("documentBinaryUrl")
                               .documentHash("documentHash")
                               .build())
            .finalWelshDocument(Document.builder()
                               .documentUrl("documentUrlWelsh")
                               .documentBinaryUrl("documentBinaryUrlWelsh")
                               .documentHash("documentHashWelsh")
                               .build())
            .build();
        Document finalDocument = serviceOfApplicationPostService.getFinalDocument(caseData);
        assertNotNull(finalDocument);
        assertEquals("documentUrlWelsh", finalDocument.getDocumentUrl());
        assertEquals("documentBinaryUrlWelsh", finalDocument.getDocumentBinaryUrl());
        assertEquals("documentHashWelsh", finalDocument.getDocumentHash());
    }

    @Test
    public void shouldReturnEmptyC1ADocumentIfNoAllegationOfHarm() {
        CaseData caseData =  CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No)
                                  .build())
            .build();
        Optional<Document> c1ADocument = serviceOfApplicationPostService.getC1aDocument(caseData);
        assertFalse(c1ADocument.isPresent());
    }

    @Test
    public void shouldReturnEnglishC1ADocumentIfAllegationOfHarmIsYes() {
        CaseData caseData =  CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .build())
            .finalDocument(Document.builder()
                               .documentUrl("documentUrl")
                               .documentBinaryUrl("documentBinaryUrl")
                               .documentHash("documentHash")
                               .build())
            .c1ADocument(Document.builder()
                             .documentUrl("documentUrl")
                             .documentBinaryUrl("documentBinaryUrl")
                             .documentHash("documentHash")
                             .build())
            .build();
        Optional<Document> c1ADocument = serviceOfApplicationPostService.getC1aDocument(caseData);
        assertTrue(c1ADocument.isPresent());
        assertEquals("documentUrl", c1ADocument.get().getDocumentUrl());
        assertEquals("documentBinaryUrl", c1ADocument.get().getDocumentBinaryUrl());
        assertEquals("documentHash", c1ADocument.get().getDocumentHash());
    }

    @Test
    public void shouldReturnWelshC1ADocumentIfAllegationOfHarmIsYes() {
        CaseData caseData =  CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .build())
            .finalDocument(Document.builder()
                               .documentUrl("documentUrl")
                               .documentBinaryUrl("documentBinaryUrl")
                               .documentHash("documentHash")
                               .build())
            .finalWelshDocument(Document.builder()
                                    .documentUrl("documentUrlWelsh")
                                    .documentBinaryUrl("documentBinaryUrlWelsh")
                                    .documentHash("documentHashWelsh")
                                    .build())
            .c1ADocument(Document.builder()
                             .documentUrl("documentUrl")
                             .documentBinaryUrl("documentBinaryUrl")
                             .documentHash("documentHash")
                             .build())
            .c1AWelshDocument(Document.builder()
                             .documentUrl("documentUrlWelsh")
                             .documentBinaryUrl("documentBinaryUrlWelsh")
                             .documentHash("documentHashWelsh")
                             .build())
            .build();
        Optional<Document> c1ADocument = serviceOfApplicationPostService.getC1aDocument(caseData);
        assertTrue(c1ADocument.isPresent());
        assertEquals("documentUrlWelsh", c1ADocument.get().getDocumentUrl());
        assertEquals("documentBinaryUrlWelsh", c1ADocument.get().getDocumentBinaryUrl());
        assertEquals("documentHashWelsh", c1ADocument.get().getDocumentHash());
    }

    @Test
    public void shouldNotGetCoverSheetInfoWhenAddressNotPresent() throws Exception {
        CaseData caseData = CaseData.builder().build();
        final Address address = Address.builder().build();
        List<Document> coversheets = serviceOfApplicationPostService.getCoverSheets(caseData,AUTH,address,"test name",
                                                                                    DOCUMENT_COVER_SHEET_HINT);
        assertEquals(0, coversheets.size());
    }

    @Test
    public void shouldReturnEmptyBulkPrintIdWhenBulkPrintServiceFails() {

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

        when(launchDarklyClient.isFeatureEnabled("soa-bulk-print")).thenReturn(true);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());
        partyDetails.setIsAddressConfidential(Yes);
        CaseData caseData = CaseData.builder().build();
        BulkPrintDetails bulkPrintOrderDetail =
            serviceOfApplicationPostService.sendPostNotificationToParty(caseData, AUTH,
                                                                        element(partyDetails), documentList, "test name");
        assertNotNull(bulkPrintOrderDetail);
        assertTrue(bulkPrintOrderDetail.getBulkPrintId().isEmpty());
        assertEquals(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build(),bulkPrintOrderDetail.getPostalAddress());
    }

    @Test
    public void shouldReturnEmptyBulkPrintIdWhenBulkPrintServiceFailsOne() {
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
        when(launchDarklyClient.isFeatureEnabled("soa-bulk-print")).thenReturn(true);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());
        partyDetails.setIsAddressConfidential(No);
        CaseData caseData = CaseData.builder().build();
        BulkPrintDetails bulkPrintOrderDetail =
            serviceOfApplicationPostService.sendPostNotificationToParty(caseData, AUTH,
                                                                        element(partyDetails), documentList, "test name");
        assertNotNull(bulkPrintOrderDetail);
        assertTrue(bulkPrintOrderDetail.getBulkPrintId().isEmpty());
        assertNotEquals(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build(),bulkPrintOrderDetail.getPostalAddress());
    }
}
