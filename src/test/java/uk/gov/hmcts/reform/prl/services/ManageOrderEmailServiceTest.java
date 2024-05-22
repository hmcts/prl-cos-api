package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
@SuppressWarnings({"java:S1607"})
public class ManageOrderEmailServiceTest {
    public static final String authToken = "Bearer TestAuthToken";

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private CourtFinderService courtFinderService;

    @InjectMocks
    private ManageOrderEmailService manageOrderEmailService;

    private Map<String, String> expectedEmailVarsAsMap;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private Court court;

    @Mock
    private ServiceArea serviceArea;

    @Mock
    private DocumentLanguageService documentLanguageService;

    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    CaseData caseData;

    PartyDetails applicant;

    PartyDetails respondent;
    PartyDetails otherPerson;
    private UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    OrganisationService organisationService;
    @Mock
    SystemUserService systemUserService;
    @Mock
    SendgridService sendgridService;

    DynamicMultiSelectList dynamicMultiSelectList;

    @Mock
    Time time;

    Document englishOrderDoc;
    Document welshOrderDoc;
    Document additionalOrderDoc;
    Document coverLetterDoc;

    @Before
    public void setUp() {


        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        otherPerson = PartyDetails.builder()
            .firstName("OtherFN")
            .lastName("OtherLN")
            .canYouProvideEmailAddress(YesOrNo.No)
            .address(Address.builder().addressLine1("#123").build())
            .build();

        coverLetterDoc = Document.builder().documentFileName("Cover_Letter").build();
        englishOrderDoc = Document.builder().documentFileName("Order_English").build();
        welshOrderDoc = Document.builder().documentFileName("Order_Welsh").build();
        additionalOrderDoc = Document.builder().documentFileName("Order_Additional").build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder()
            .id(uuid)
            .value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
            .id(uuid)
            .value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .build();

        List<Court> courtList = new ArrayList<>();
        courtList.add(court);

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());
    }

    @Test
    public void sendEmail() throws NotFoundException {


        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);

        manageOrderEmailService.sendEmail(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void buildCafcassEmailNotificationForC100() throws NotFoundException {


        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .isCaseUrgent(YesOrNo.No)
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("PRL-ORDER-C21-COMMON.docx")
                .build())
            .familymanCaseNumber("12345678")
            .issueDate(LocalDate.parse("2022-02-16"))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertNotNull(manageOrderEmailService.buildEmailToCafcassAndOtherParties(caseData));
    }

    @Test
    public void buildCafcassEmailNotificationForFL401() throws NotFoundException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .isCaseUrgent(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("PRL-ORDER-C21-COMMON.docx")
                .build())
            .fl401FamilymanCaseNumber("12345678")
            .issueDate(LocalDate.parse("2022-02-16"))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertNotNull(manageOrderEmailService.buildEmailToCafcassAndOtherParties(caseData));
    }

    @Test
    public void sendCafcassEmailNotification() throws NotFoundException {


        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        String otherEmail = "testing@other.com";

        Element<String> wrappedOther = Element.<String>builder().value(otherEmail).build();
        List<Element<String>> listOfOtherEmail = Collections.singletonList(wrappedOther);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .otherEmailAddress(listOfOtherEmail)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .isCaseUrgent(YesOrNo.No)
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("PRL-ORDER-C21-COMMON.docx")
                .build())
            .fl401FamilymanCaseNumber("12345678")
            .issueDate(LocalDate.parse("2022-02-16"))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        manageOrderEmailService.sendEmailToCafcassAndOtherParties(caseDetails);
        assertEquals(listOfCafcassEmail, caseData.getManageOrders().getCafcassEmailAddress());
        assertEquals(listOfOtherEmail, caseData.getManageOrders().getOtherEmailAddress());
    }

    @Test
    public void verifyEmailNotificationTriggeredForFinalOrderIssued() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .state(State.ALL_FINAL_ORDERS_ISSUED)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .build())))
            .respondents(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyNoEmailNotificationTriggeredIfStateIsNotAllOrderIssued() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .build())))
            .respondents(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(0)).send(Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyEmailNotificationTriggeredForFinalOrderIssuedBuildRespondentEmail() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .state(State.ALL_FINAL_ORDERS_ISSUED)
            .applicants(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .email("test@gmail.com")
                .build())))
            .respondents(List.of(element(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .email("test@gmail.com")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .build())))
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(3)).send(Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),Mockito.any());
    }

    @Test
    public void verifyEmailNotificationTriggeredForFinalOrderIssuedBuildRespondentEmailFl401() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("Fl401")
            .state(State.ALL_FINAL_ORDERS_ISSUED)
            .respondentsFL401(PartyDetails.builder()
                .solicitorEmail("test@gmail.com")
                .representativeLastName("LastName")
                .representativeFirstName("FirstName")
                .email("test@gmail.com")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .build())
            .build();


        CaseDetails caseDetails = CaseDetails.builder().build();
        Mockito.when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        manageOrderEmailService.sendFinalOrderIssuedNotification(caseDetails);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),Mockito.any());
    }

    @Test
    public void sendEmailWhenOrderIsServed() {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();
        uuid = UUID.fromString(TEST_UUID);
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().id(uuid).value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(uuid).value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code("00000000-0000-0000-0000-000000000000")
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .cafcassCymruServedOptions(YesOrNo.Yes)
            .cafcassServedOptions(YesOrNo.Yes)
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .build();

        childLiveWithList.add(LiveWithEnum.applicant);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testServeOrdersToOtherOrganisation() {
        PostalInformation address = PostalInformation.builder()
            .postalAddress(Address.builder()
                .addressLine1("Made Up Street").build())
            .postalName("Test")
            .build();
        Element<PostalInformation> wrappedAddress = Element.<PostalInformation>builder()
            .id(uuid)
            .value(address).build();
        List<Element<PostalInformation>> listOfAddress = Collections.singletonList(wrappedAddress);

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .typeOfOrder("Final")
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .otherPartiesServed(YesOrNo.Yes)
                .postalInformation(listOfAddress)
                .build())
            .build();
        List<Element<PartyDetails>> parties = new ArrayList<>();
        parties.add(element(PartyDetails.builder().build()));
        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .caseTypeOfApplication("C100")
            .applicants(parties)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .serveToRespondentOptions(YesOrNo.Yes)
                              .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
                              .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testServeOrdersToOtherOrganisationThrowsException() throws Exception {
        PostalInformation address = PostalInformation.builder()
            .postalAddress(Address.builder()
                .addressLine1("Made Up Street").build())
            .postalName("Test")
            .build();
        Element<PostalInformation> wrappedAddress = Element.<PostalInformation>builder()
            .id(uuid)
            .value(address).build();
        List<Element<PostalInformation>> listOfAddress = Collections.singletonList(wrappedAddress);

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .typeOfOrder("Final")
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .otherPartiesServed(YesOrNo.Yes)
                .postalInformation(listOfAddress)
                .build())
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(caseData,"testAuth", address.getPostalAddress(),"Test",
            PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT))
            .thenThrow(new RuntimeException());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("testAuth", caseData, dataMap);
        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testServeOrdersToOtherOrganisationServeOrderDetailsNull() {
        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .typeOfOrder("Final")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testServeOrdersToOtherOrganisationAddressIsEmpty() {
        PostalInformation address = PostalInformation.builder()
            .postalName("Test")
            .build();
        Element<PostalInformation> wrappedAddress = Element.<PostalInformation>builder()
            .id(uuid)
            .value(address).build();
        List<Element<PostalInformation>> listOfAddress = Collections.singletonList(wrappedAddress);

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .otherPartiesServed(YesOrNo.Yes)
                .postalInformation(listOfAddress)
                .build())
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .serveToRespondentOptions(YesOrNo.No)
                .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testServeOrdersToOtherOrganisationPostalInformationIsEmpty() {
        PostalInformation address = PostalInformation.builder()
            .postalName("Test")
            .postalAddress(Address.builder().build())
            .build();
        Element<PostalInformation> wrappedAddress = Element.<PostalInformation>builder()
            .id(uuid)
            .value(address).build();
        List<Element<PostalInformation>> listOfAddress = Collections.singletonList(wrappedAddress);

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .otherPartiesServed(YesOrNo.Yes)
                .postalInformation(listOfAddress)
                .build())
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testServeOrdersDetailsNull() {
        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(LocalDateTime.now())
            .orderDocument(englishOrderDoc)
            .orderDocumentWelsh(welshOrderDoc)
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(additionalOrderDoc)))
                .otherPartiesServed(YesOrNo.Yes)
                .build())
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .serveToRespondentOptions(YesOrNo.Yes)
                .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                .build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();

        Map<String, Object> dataMap = new HashMap<>();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
    }

    @Test
    public void testSendEmailWhenOrderServed_General_Order() throws IOException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .issueDate(LocalDate.now())
            .othersToNotify(List.of(Element.<PartyDetails>builder().id(uuid)
                .value(PartyDetails.builder()
                    .canYouProvideEmailAddress(YesOrNo.Yes)
                    .email("test")
                    .build()).build()))
            .manageOrders(ManageOrders.builder()
                .cafcassCymruServedOptions(YesOrNo.Yes)
                .cafcassEmailAddress(List.of(element("test")))
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                .deliveryByOptionsCA(DeliveryByEnum.email)
                .emailInformationCA(List.of(Element.<EmailInformation>builder()
                    .id(uuid)
                    .value(EmailInformation
                        .builder()
                        .emailAddress("test")
                        .build())
                    .build()))
                .otherParties(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(Element.<OrderDetails>builder()
                .id(uuid)
                .value(OrderDetails.builder().serveOrderDetails(ServeOrderDetails.builder().additionalDocuments(
                        List.of(element(Document.builder().build()))).build())
                    .typeOfOrder(SelectTypeOfOrderEnum.general.getDisplayedValue()).build())
                .build()))
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(sendgridService.sendEmailUsingTemplateWithAttachments(any(SendgridEmailTemplateNames.class),
                                                                                anyString(),
                                                                                any(SendgridEmailConfig.class))).thenReturn(true);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, dataMap);

        Mockito.verifyNoInteractions(emailService);
    }

    @Test
    public void testSendEmailWhenOrderServed_two_Orders() throws IOException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElementTwo = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000001")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement,dynamicMultiselectListElementTwo))
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .issueDate(LocalDate.now())
            .othersToNotify(List.of(Element.<PartyDetails>builder().id(uuid)
                .value(PartyDetails.builder()
                    .canYouProvideEmailAddress(YesOrNo.Yes)
                    .email("test")
                    .build()).build()))
            .manageOrders(ManageOrders.builder()
                .cafcassCymruServedOptions(YesOrNo.Yes)
                .cafcassEmailAddress(List.of(element("test")))
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                .deliveryByOptionsCA(DeliveryByEnum.email)
                .emailInformationCA(List.of(Element.<EmailInformation>builder()
                    .id(uuid)
                    .value(EmailInformation
                        .builder()
                        .emailAddress("test")
                        .build())
                    .build()))
                .otherParties(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(Element.<OrderDetails>builder()
                .id(uuid)
                .value(OrderDetails.builder().serveOrderDetails(ServeOrderDetails.builder().additionalDocuments(
                        List.of(element(Document.builder().build()))).build())
                    .typeOfOrder(SelectTypeOfOrderEnum.general.getDisplayedValue()).build())
                .build(),Element.<OrderDetails>builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .value(OrderDetails.builder().serveOrderDetails(ServeOrderDetails.builder().additionalDocuments(
                        List.of(element(Document.builder().build()))).build())
                    .typeOfOrder(SelectTypeOfOrderEnum.finl.getDisplayedValue()).build())
                .build()))
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(sendgridService.sendEmailUsingTemplateWithAttachments(any(SendgridEmailTemplateNames.class),
                                                                                anyString(),
                                                                                any(SendgridEmailConfig.class))).thenReturn(true);

        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, dataMap);
        Mockito.verifyNoInteractions(emailService);
    }

    @Test
    public void testSendEmailWhenOrderServed_Interim_Order() throws IOException {
        CaseDetails caseDetails = CaseDetails.builder().build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .issueDate(LocalDate.now())
            .othersToNotify(List.of(Element.<PartyDetails>builder().id(uuid)
                .value(PartyDetails.builder()
                    .canYouProvideEmailAddress(YesOrNo.Yes)
                    .email("test")
                    .build()).build()))
            .manageOrders(ManageOrders.builder()
                .cafcassCymruServedOptions(YesOrNo.Yes)
                .cafcassEmailAddress(List.of(element("test")))
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
                .deliveryByOptionsCA(DeliveryByEnum.email)
                .emailInformationCA(List.of(Element.<EmailInformation>builder()
                    .id(uuid)
                    .value(EmailInformation
                        .builder()
                        .emailAddress("test")
                        .build())
                    .build()))
                .otherParties(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .orderCollection(List.of(Element.<OrderDetails>builder()
                .id(uuid)
                .value(OrderDetails.builder().serveOrderDetails(ServeOrderDetails.builder().additionalDocuments(
                        List.of(element(Document.builder().build()))).build())
                    .typeOfOrder(SelectTypeOfOrderEnum.interim.getDisplayedValue()).build())
                .build()))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(sendgridService.sendEmailUsingTemplateWithAttachments(any(SendgridEmailTemplateNames.class),
                                                                                anyString(),
                                                                                any(SendgridEmailConfig.class)))
            .thenReturn(true);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, dataMap);

        Mockito.verifyNoInteractions(emailService);
    }


    @Test
    public void testSendEmailWhenOrderServedShouldInvoke() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .contactPreferences(ContactPreferences.email)
            .solicitorEmail("")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .cafcassEmailId("test")
                .serveOrderDynamicList(dynamicMultiSelectList).build())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(sendgridService,Mockito.times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(),
            Mockito.any(),
            Mockito.any());
    }

    @Test
    public void testSendEmailForCitizenWhenTheyHaveDashboardAccess() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .build();
        PartyDetails respondent = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .user(User.builder().idamId("123").build())
            .contactPreferences(ContactPreferences.email)
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(uuid).value(respondent).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(OrderDetails.builder().typeOfOrder("Interim").build())))
            .welshLanguageRequirement(YesOrNo.Yes)
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(emailService,Mockito.times(1)).send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeForRespondentContactPrefDigital() throws Exception {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .build();
        PartyDetails respondent = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .user(User.builder().build())
            .contactPreferences(ContactPreferences.email)
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(uuid).value(respondent).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(sendgridService,Mockito.times(1)).sendEmailUsingTemplateWithAttachments(Mockito.any(),
            Mockito.any(),
            Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeForRespondentContactPrefPost() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        LocalDateTime now = LocalDateTime.of(2023,8, 23, 0, 0, 0);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .build();
        PartyDetails respondent = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .user(User.builder().build())
            .contactPreferences(ContactPreferences.email)
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(now)
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(Document.builder().build())))
                .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(uuid).value(respondent).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(uuid,orderDetails)))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(sendgridService,Mockito.times(1)).sendEmailUsingTemplateWithAttachments(Mockito.any(),
            Mockito.any(),
            Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl401() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectListFl401 = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.post)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .postalInformation(PostalInformation.builder()
                        .postalAddress(Address.builder().build())
                        .build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectListFl401)
                .cafcassEmailId("test").build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl401Welsh() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.post)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .postalInformation(PostalInformation.builder()
                        .postalAddress(Address.builder().build())
                        .build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .build();
        Map<String, Object> dataMap = new HashMap<>();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl401ServeOtherPartiesDaNull() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectListFl401Null = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(null)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectListFl401Null)
                .cafcassEmailId("test").build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl401ServeOtherPartiesDaNullWelsh() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(null)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .build();
        Map<String, Object> dataMap = new HashMap<>();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl40ServeOtherPartiesNotSetToOther() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectListFl401Other = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of())
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectListFl401Other)
                .cafcassEmailId("test").build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl40ServeOtherPartiesNotSetToOtherWelsh() throws IOException {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of())
                .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                    .applicantLegalRepresentative)
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .build();
        Map<String, Object> dataMap = new HashMap<>();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(sendgridService,Mockito.times(1))
            .sendEmailUsingTemplateWithAttachments(Mockito.any(), any(), any());
    }

    @Test
    public void testSendEmailWhenOrderServed() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
                .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder()
                    .serveByPostOrEmail(DeliveryByEnum.email)
                    .emailInformation(EmailInformation.builder()
                        .emailAddress("test").build())
                    .build())))
                .recipientsOptions(dynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .build();
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(emailService,Mockito.times(0)).send(Mockito.any(), any(), any(), any());
    }


    @Test
    public void testSendOrderAndAdditionalDocsToOtherPersonViaPost() throws Exception {
        //Given
        caseData = caseData.toBuilder()
            .othersToNotify(List.of(element(uuid, otherPerson)))
            .manageOrders(ManageOrders.builder()
                .serveOrderDynamicList(dynamicMultiSelectList)
                .otherParties(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(caseData, authToken, otherPerson.getAddress(),
            otherPerson.getLabelForDynamicList(),
            PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT))
            .thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(String.valueOf(caseData.getId()), authToken, "OrderPack",
            List.of(coverLetterDoc, englishOrderDoc, welshOrderDoc, additionalOrderDoc),
            otherPerson.getLabelForDynamicList())).thenReturn(uuid);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(1, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails().get(0).getValue().getBulkPrintId());
    }

    @Test
    public void testSendOrderAndAdditionalDocsToRespondentViaPost() throws Exception {

        //Given
        PartyDetails respondent = PartyDetails.builder()
            .firstName("RespFN")
            .lastName("RespLN")
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder().addressLine1("#123").build())
            .build();
        caseData = caseData.toBuilder()
            .respondents(List.of(element(uuid, respondent)))
            .manageOrders(ManageOrders.builder()
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .recipientsOptions(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(caseData, authToken, respondent.getAddress(),
            respondent.getLabelForDynamicList(),
            PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT))
            .thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(String.valueOf(caseData.getId()), authToken, "OrderPack",
            List.of(coverLetterDoc, englishOrderDoc, welshOrderDoc, additionalOrderDoc),
            respondent.getLabelForDynamicList())).thenReturn(uuid);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(1, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails().get(0).getValue().getBulkPrintId());
    }

    @Test
    public void testServeOrderDocsToRespondentsEmailOtherPersonPost() throws Exception {
        //Given
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("RespFN")
            .lastName("RespLN")
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder().addressLine1("#123").build())
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("RespFN2")
            .lastName("RespLN2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .contactPreferences(ContactPreferences.email)
            .build();

        String uuid1 = "00000000-0000-0000-0000-000000000000";
        String uuid2 = "00000000-0000-0000-0000-000000000001";
        DynamicMultiselectListElement dynamicMultiselectListElement1 = DynamicMultiselectListElement
            .builder()
            .code(uuid1)
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement2 = DynamicMultiselectListElement
            .builder()
            .code(uuid2)
            .build();
        DynamicMultiSelectList receipientDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement1, dynamicMultiselectListElement2))
            .build();

        caseData = caseData.toBuilder()
            .respondents(List.of(element(uuid, respondent1),
                element(UUID.fromString(uuid2), respondent2)))
            .othersToNotify(List.of(element(uuid, otherPerson)))
            .manageOrders(ManageOrders.builder()
                .serveToRespondentOptions(YesOrNo.No)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .otherParties(dynamicMultiSelectList)
                .recipientsOptions(receipientDynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(uuid);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(2, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());

    }

    @Test
    public void sendEmailWhenOrderIsServedOnly47aNoOptionsSelected() {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .contactPreferences(ContactPreferences.post)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .contactPreferences(ContactPreferences.post)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();
        uuid = UUID.fromString(TEST_UUID);
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().id(uuid).value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(uuid).value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code("00000000-0000-0000-0000-000000000000")
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        childLiveWithList.add(LiveWithEnum.applicant);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .issueDate(LocalDate.now())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testsendEmailToLegalRepresentativeOnRejection() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .manageOrders(ManageOrders.builder().instructionsToLegalRepresentative("test").build())
            .applicantSolicitorEmailAddress("test@test.com")
            .issueDate(LocalDate.now())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        manageOrderEmailService.sendEmailToLegalRepresentativeOnRejection(caseDetails, DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder().orderCreatedBy("Solicitor name").build()).build());
        verify(emailService, times(1)).getCaseData(caseDetails);
    }

    @Test
    public void sendEmailWhenOrderIsServedOnly47a() {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();
        uuid = UUID.fromString(TEST_UUID);
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().id(uuid).value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(uuid).value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code("00000000-0000-0000-0000-000000000000")
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOtherPartiesCA(List.of())
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        childLiveWithList.add(LiveWithEnum.applicant);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .issueDate(LocalDate.now())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .build();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void sendEmailWhenOrderIsServedEmailOptionIsEmpty() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .applicantLegalRepresentative)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendEmailWhenOrderIsServedEmailOptionIsEmptyWelsh() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectListEmailOptionEmpty = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectListEmailOptionEmpty)
            .serveOrderDynamicList(dynamicMultiSelectListEmailOptionEmpty)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .applicantLegalRepresentative)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectListEmailOptionEmpty)
            .build();

        CaseData caseDataEmailOption = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDataEmailOption.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseDataEmailOption);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseDataEmailOption, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtBailiffOptionSelected() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .courtBailiff)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtBailiffOptionSelectedWelsh() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectListCourtBaliff = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectListCourtBaliff)
            .serveOrderDynamicList(dynamicMultiSelectListCourtBaliff)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .courtBailiff)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectListCourtBaliff)
            .build();

        CaseData caseDataCourtBaliff = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDataCourtBaliff.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseDataCourtBaliff);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseDataCourtBaliff, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtAdminOptionSelected() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .courtAdmin)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtAdminOptionSelectedWelsh() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectListCourtAdmin = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectListCourtAdmin)
            .serveOrderDynamicList(dynamicMultiSelectListCourtAdmin)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                .courtAdmin)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectListCourtAdmin)
            .build();

        CaseData caseDataCourtAdmin = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDataCourtAdmin.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseDataCourtAdmin);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseDataCourtAdmin, dataMap);
        verify(sendgridService, times(2)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendEmailWhenOrderIsServedToCafcassCymruWelsh() throws IOException {

        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code("00000000-0000-0000-0000-000000000000")
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.Yes)
            .cafcassCymruServedOptions(YesOrNo.Yes)
            .otherParties(dynamicMultiSelectList)
            .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
            .cafcassCymruEmail("test@cafcasscymru.com")
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }



    @Test
    public void sendServeOrderEmailWhenCourtBailiffOptionSelectedForC100Case() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum
                .courtBailiff)
            .serveToRespondentOptions(YesOrNo.Yes)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtBailiffOptionSelectedForC100CaseWelsh() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectListC100Welsh = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectListC100Welsh)
            .serveOrderDynamicList(dynamicMultiSelectListC100Welsh)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum
                .courtBailiff)
            .serveToRespondentOptions(YesOrNo.Yes)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectListC100Welsh)
            .build();

        CaseData caseDataC100Welsh = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDataC100Welsh.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseDataC100Welsh);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseDataC100Welsh, dataMap);
        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void sendServeOrderEmailWhenCourtAdminOptionSelectedForC100Case() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectList)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum
                .courtAdmin)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectList)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void sendServeOrderEmailWhenCourtAdminOptionSelectedForC100CaseWelsh() throws IOException {
        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        DynamicMultiSelectList dynamicMultiSelectListCaWelsh = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                .label("John (Child 1)")
                .code(TEST_UUID)
                .build())).build();
        ManageOrders manageOrders = ManageOrders.builder()
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(dynamicMultiSelectListCaWelsh)
            .serveOrderDynamicList(dynamicMultiSelectListCaWelsh)
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum
                .courtAdmin)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                .emailInformation(EmailInformation.builder().emailName("").build())
                .build())))
            .otherParties(dynamicMultiSelectListCaWelsh)
            .build();

        CaseData caseDataCaWelsh = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .solicitorEmail("t")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                .lastName("test")
                .firstName("test1")
                .email("test@sdsc.com").build())
            .children(listOfChildren)
            .orderCollection(List.of(element(UUID.fromString(TEST_UUID),OrderDetails.builder().build())))
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDataCaWelsh.getId())
            .data(dataMap)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseDataCaWelsh);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.TRUE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseDataCaWelsh, dataMap);
        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeServeOrderToApplicantAddress() throws Exception {

        CaseDetails caseDetails = CaseDetails.builder().build();
        LocalDateTime now = LocalDateTime.of(2023, 8, 23, 0, 0, 0);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .contactPreferences(ContactPreferences.post)
            .solicitorEmail("")
            .address(Address.builder().addressLine1("addressLine1").build())
            .build();

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(now)
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(Document.builder().build())))
                .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(uuid, orderDetails)))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        UUID bulkPrintId = UUID.randomUUID();
        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString()))
            .thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(bulkPrintId);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) dataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(1, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails().get(0).getValue().getBulkPrintId());
        assertEquals(
            bulkPrintId.toString(),
            orderCollection.get(0).getValue().getBulkPrintOrderDetails().get(0).getValue().getBulkPrintId()
        );

        Mockito.verify(sendgridService, Mockito.times(1)).sendEmailUsingTemplateWithAttachments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        );
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeServeOrderToApplicantAddressWithNoAddress() throws Exception {

        CaseDetails caseDetails = CaseDetails.builder().build();
        LocalDateTime now = LocalDateTime.of(2023, 8, 23, 0, 0, 0);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .address(Address.builder().build())
            .build();

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(now)
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(Document.builder().build())))
                .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(uuid, orderDetails)))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        UUID bulkPrintId = UUID.randomUUID();
        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(
            coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(bulkPrintId);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) dataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(0, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeServeOrderToApplicantAddressException() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder().build();
        LocalDateTime now = LocalDateTime.of(2023, 8, 23, 0, 0, 0);
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code("00000000-0000-0000-0000-000000000000")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(dynamicMultiselectListElement))
            .build();
        DynamicMultiselectListElement serveOrderDynamicMultiselectListElement = DynamicMultiselectListElement
            .builder()
            .code(uuid.toString())
            .build();
        DynamicMultiSelectList serveOrderDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(serveOrderDynamicMultiselectListElement))
            .build();
        applicant = applicant.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("")
            .representativeFirstName("")
            .solicitorEmail("")
            .address(Address.builder().addressLine1("addressLine1").build())
            .build();

        OrderDetails orderDetails = OrderDetails.builder()
            .orderTypeId("abc")
            .dateCreated(now)
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                .additionalDocuments(List.of(element(Document.builder().build())))
                .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(Element.<PartyDetails>builder().id(uuid).value(applicant).build()))
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                .serveToRespondentOptions(YesOrNo.No)
                .recipientsOptions(dynamicMultiSelectList)
                .serveOrderDynamicList(serveOrderDynamicMultiSelectList)
                .cafcassEmailId("test").build())
            .orderCollection(List.of(element(uuid, orderDetails)))
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(
            coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenThrow(new RuntimeException());
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> dataMap = new HashMap<>();
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        assertNotNull(dataMap.get("orderCollection"));
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) dataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(0, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
        assertEquals(new ArrayList<>(), orderCollection.get(0).getValue().getBulkPrintOrderDetails());
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaPostC100() throws Exception {
        //Given
        PartyDetails applicantPost = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder().addressLine1("#123").build())
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicantPost)))
            .manageOrders(ManageOrders.builder()
                .serveToRespondentOptions(YesOrNo.Yes)
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(uuid);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(1, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantWithNoAddressC100() {
        //Given
        PartyDetails applicantNoAddress = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicantNoAddress)))
            .manageOrders(ManageOrders.builder()
                .serveToRespondentOptions(YesOrNo.Yes)
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertTrue(orderCollection.get(0).getValue().getBulkPrintOrderDetails().isEmpty());
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaEmailC100() {
        //Given
        PartyDetails applicantEmail = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .address(Address.builder().addressLine1("#123").build())
            .contactPreferences(ContactPreferences.email)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicantEmail)))
            .manageOrders(ManageOrders.builder()
                .serveToRespondentOptions(YesOrNo.Yes)
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //ADD MORE ASSERTIONS WHEN EMAIL IS IMPLEMENTED
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaPostFL401() throws Exception {
        //Given
        PartyDetails applicantPostFl401 = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder().addressLine1("#123").build())
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantPostFl401)
            .manageOrders(ManageOrders.builder()
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), any())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(uuid);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(1, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantWithNoAddressFL401() {
        //Given
        PartyDetails applicantNoAddressFl401 = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantNoAddressFl401)
            .manageOrders(ManageOrders.builder()
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertTrue(orderCollection.get(0).getValue().getBulkPrintOrderDetails().isEmpty());
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaEmailFL401() throws Exception {
        //Given
        PartyDetails applicantEmailFl401 = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .address(Address.builder().addressLine1("#123").build())
            .contactPreferences(ContactPreferences.email)
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantEmailFl401)
            .manageOrders(ManageOrders.builder()
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(uuid);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //ADD MORE ASSERTIONS WHEN EMAIL IS IMPLEMENTED
    }


    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaEmailFL401ThrowException() throws Exception {
        //Given
        PartyDetails applicantFl401Exception = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .address(Address.builder().addressLine1("#123").build())
            .contactPreferences(ContactPreferences.email)
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFl401Exception)
            .manageOrders(ManageOrders.builder()
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), anyString())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenThrow(new RuntimeException());
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //ADD MORE ASSERTIONS WHEN EMAIL IS IMPLEMENTED
    }

    @Test
    public void testServeOrderDocsToUnrepresentedApplicantViaEmailDa() {
        //Given
        PartyDetails applicantDa = PartyDetails.builder()
            .partyId(uuid)
            .firstName("AppFN")
            .lastName("AppLN")
            .address(Address.builder().addressLine1("#123").build())
            .contactPreferences(ContactPreferences.email)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("abc@test.com")
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantDa)
            .manageOrders(ManageOrders.builder()
                .displayLegalRepOption(PrlAppsConstants.NO)
                .servingOptionsForNonLegalRep(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .serveOrderDynamicList(dynamicMultiSelectList)
                .build())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(Boolean.TRUE).isGenWelsh(Boolean.FALSE).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        Map<String, Object> caseDataMap = new HashMap<>();
        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //ADD MORE ASSERTIONS WHEN EMAIL IS IMPLEMENTED
    }
}
