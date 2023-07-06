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
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
    }

    @Test
    public void givenOnlyRepresentedRespondents_thenNoPostSent() throws Exception {
        PartyDetails respondent1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(element(respondent1), element(respondent2)))
            .build();

        serviceOfApplicationPostService.send(caseData, AUTH);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void givenCaseWithNoAllegationsOfHarm_sentDocsDoesNotContainC1a() throws Exception {
        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
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
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.No).build())
            .finalDocument(finalDoc)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);
        List<Document> sentDocs = serviceOfApplicationPostService.send(caseData, AUTH);
        assertTrue(sentDocs.contains(
            finalDoc
        ));
    }


    @Test
    public void givenCaseWithAllegationsOfHarm_generateAllWelshDocs() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalWelshDoc = Document.builder()
            .documentUrl("finalWelshDoc")
            .documentBinaryUrl("finalWelshDoc")
            .documentHash("finalWelshDoc")
            .build();

        Document finalWelshC1a = Document.builder()
            .documentUrl("finalWelshC1a")
            .documentBinaryUrl("finalWelshC1a")
            .documentHash("finalWelshC1a")
            .build();

        PartyDetails respondent1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .solicitorEmail("test@gmail.com")
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail("test@gmail.com")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalWelshDocument(finalWelshDoc)
            .c1AWelshDocument(finalWelshC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent1), element(respondent2)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<Document> sentDocs = serviceOfApplicationPostService.send(caseData, AUTH);
        assertTrue(sentDocs.contains(finalWelshDoc));
    }

    @Test
    public void givenCaseWithAllegationsOfHarm_generateAllEnglishDocs() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document finalC1a = Document.builder()
            .documentUrl("finalC1a")
            .documentBinaryUrl("finalC1a")
            .documentHash("finalC1a")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalDocument(finalDoc)
            .c1ADocument(finalC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<Document> sentDocs = serviceOfApplicationPostService.send(caseData, AUTH);
        assertTrue(sentDocs.contains(finalDoc));
    }

    @Test
    public void givenCaseWithMultipleOrders_generateAllEnglishDocsContainingOrders() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document finalC1a = Document.builder()
            .documentUrl("finalC1a")
            .documentBinaryUrl("finalC1a")
            .documentHash("finalC1a")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();


        Document standardDirectionsOrder = Document.builder()
            .documentUrl("standardDirectionsOrder")
            .documentBinaryUrl("standardDirectionsOrder")
            .documentHash("standardDirectionsOrder")
            .build();

        OrderDetails standardDirectionsOrderDetails = OrderDetails.builder()
            .orderType("Standard directions order")
            .orderTypeId("standardDirectionsOrder")
            .orderDocument(standardDirectionsOrder)
            .build();

        List<Element<OrderDetails>> orderCollection = new ArrayList<>();
        orderCollection.add(element(standardDirectionsOrderDetails));

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalDocument(finalDoc)
            .c1ADocument(finalC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .respondents(List.of(element(respondent)))
            .orderCollection(orderCollection)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<Document> sentDocs = serviceOfApplicationPostService.send(caseData, AUTH);
        assertTrue(sentDocs.containsAll(List.of(finalDoc,finalC1a)));
    }

    @Test
    public void givenPeopleInTheCaseWithAddress() throws Exception {

        Document privacyNotice = Document.builder()
            .documentUrl("privacyNotice")
            .documentBinaryUrl("privacyNotice")
            .documentHash("privacyNotice")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(privacyNotice);

        PartyDetails otherPeopleInTheCase = PartyDetails.builder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder().addressLine1("test").postCode("test").build())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .othersToNotify(List.of(element(otherPeopleInTheCase)))
            .serviceOfApplicationScreen1(DynamicMultiSelectList.builder().build())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        assertNotNull(serviceOfApplicationPostService.sendDocs(caseData, AUTH));
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
