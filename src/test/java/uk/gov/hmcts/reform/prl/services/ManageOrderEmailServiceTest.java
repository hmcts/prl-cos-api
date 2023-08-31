package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrderEmailServiceTest {

    private static final String manageCaseUrl = null;
    public static final String authToken = "Bearer TestAuthToken";
    private static final String URGENT_CASE = "Urgent ";

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

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

    Document englishOrderDoc;
    Document welshOrderDoc;
    Document additionalOrderDoc;
    Document coverLetterDoc;

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

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

        String childNames = "child1 child2";

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
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

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

        String childNames = "child1 child2";

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

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String typeOfHearing = " ";

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(caseData.getFamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentFileName())
            .build();

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

        String childNames = "child1 child2";

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

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String typeOfHearing = " ";

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(caseData.getFamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentFileName())
            .build();

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

        String childNames = "child1 child2";

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

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String typeOfHearing = " ";

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(caseData.getFamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentUrl())
            .build();

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        manageOrderEmailService.sendEmailToCafcassAndOtherParties(caseDetails);
        assertEquals(listOfCafcassEmail, caseData.getManageOrders().getCafcassEmailAddress());
        assertEquals(listOfOtherEmail, caseData.getManageOrders().getOtherEmailAddress());
    }

    @Test
    public void sendEmailNotificationToCA_ApplicantAndRespondents() {
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

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
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
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

        manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }



    @Test
    public void sendEmailNotificationToFL401_ApplicantAndRespondents() {
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

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                    .lastName("test")
                                 .firstName("test1")
                                 .email("test@ree.com").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                    .lastName("test")
                                  .firstName("test1")
                                  .email("test@sdsc.com").build())
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

        manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }


    @Test
    public void sendEmailNotificationFinalOrderToCA_ApplicantAndRespondents() {
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

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

        manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void sendEmailNotificationToFinalOrderFL401_ApplicantAndRespondents() {
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

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("FL401")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .applicantsFL401(PartyDetails.builder()
                                 .lastName("test")
                                 .firstName("test1").build())
            .respondents(listOfRespondents)
            .respondentsFL401(PartyDetails.builder()
                                  .solicitorEmail("")
                                  .representativeLastName("")
                                  .representativeFirstName("")
                                  .lastName("test")
                                  .firstName("test1").build())
            .children(listOfChildren)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .courtName("testcourt")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

        manageOrderEmailService.sendEmailToApplicantAndRespondent(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void verifyEmailNotificationTriggeredForFinalOrderIssued() throws  Exception {
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
    public void verifyNoEmailNotificationTriggeredIfStateIsNotAllOrderIssued() throws  Exception {
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
    public void verifyEmailNotificationTriggeredForFinalOrderIssuedBuildRespondentEmail() throws  Exception {
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
    public void verifyEmailNotificationTriggeredForFinalOrderIssuedBuildRespondentEmailFl401() throws  Exception {
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
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();
        String applicantNames = "TestFirst TestLast";

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink("/dummyURL")
            .build();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testSendEmailWhenOrderServed() {
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
                .orderCollection(List.of(element(OrderDetails.builder().build())))
                .build();
        Map<String, Object> dataMap = new HashMap<>();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(emailService,Mockito.times(2)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
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
        when(serviceOfApplicationPostService
            .getCoverLetterGeneratedDocInfo(any(CaseData.class), anyString(),
                                            any(Address.class),
                                            anyString()
            )).thenReturn(GeneratedDocumentInfo.builder().build());
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(emailService,Mockito.times(2)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvokeForRespondentContactPrefDigital() throws Exception {
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
            .user(User.builder().idamId("abc123").build())
            .contactPreferences(ContactPreferences.digital)
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
        when(serviceOfApplicationPostService
                 .getCoverLetterGeneratedDocInfo(any(CaseData.class), anyString(),
                                                 any(Address.class),
                                                 anyString()
                 )).thenReturn(GeneratedDocumentInfo.builder().build());
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(emailService,Mockito.times(3)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
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
            .user(User.builder().idamId("abc123").build())
            .contactPreferences(ContactPreferences.post)
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
        when(serviceOfApplicationPostService
                 .getCoverLetterGeneratedDocInfo(any(CaseData.class), anyString(),
                                                 any(Address.class),
                                                 anyString()
                 )).thenReturn(GeneratedDocumentInfo.builder().build());
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);

        Mockito.verify(emailService,Mockito.times(2)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedFl401() {
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
            .solicitorEmail("test@gmail.com")
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("Fl401")
            .applicantsFL401(applicant)
            .respondentsFL401(applicant)
            .issueDate(LocalDate.now())
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes)
                              .serveToRespondentOptions(YesOrNo.No)
                              .recipientsOptions(dynamicMultiSelectList)
                              .cafcassEmailId("test").build())
            .build();
        Map<String, Object> dataMap = new HashMap<>();

        manageOrderEmailService.sendEmailWhenOrderIsServed("tesAuth", caseData, dataMap);
        Mockito.verify(emailService,Mockito.times(2)).send(Mockito.any(), any(), any(), any());
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
        Map<String, Object> caseDataMap = new HashMap<>();

        when(serviceOfApplicationPostService.getCoverLetter(caseData, authToken, otherPerson.getAddress(),
                                                            otherPerson.getLabelForDynamicList())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(String.valueOf(caseData.getId()), authToken, "OrderPack",
                                   List.of(coverLetterDoc, englishOrderDoc, welshOrderDoc, additionalOrderDoc),
                                   otherPerson.getLabelForDynamicList())).thenReturn(uuid);

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

        Map<String, Object> caseDataMap = new HashMap<>();

        when(serviceOfApplicationPostService.getCoverLetter(caseData, authToken, respondent.getAddress(),
                                                            respondent.getLabelForDynamicList())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(String.valueOf(caseData.getId()), authToken, "OrderPack",
                                   List.of(coverLetterDoc, englishOrderDoc, welshOrderDoc, additionalOrderDoc),
                                   respondent.getLabelForDynamicList())).thenReturn(uuid);

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
            .address(Address.builder().addressLine1("#123").build())
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("RespFN2")
            .lastName("RespLN2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .contactPreferences(ContactPreferences.digital)
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

        Map<String, Object> caseDataMap = new HashMap<>();

        when(serviceOfApplicationPostService.getCoverLetter(any(), any(), any(), any())).thenReturn(List.of(coverLetterDoc));
        when(bulkPrintService.send(any(), any(), any(), anyList(), any())).thenReturn(uuid);

        //When
        manageOrderEmailService.sendEmailWhenOrderIsServed(authToken, caseData, caseDataMap);

        //Then
        assertNotNull(caseDataMap.get("orderCollection"));
        //noinspection unchecked
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) caseDataMap.get("orderCollection");
        assertNotNull(orderCollection.get(0).getValue().getBulkPrintOrderDetails());
        assertEquals(2, orderCollection.get(0).getValue().getBulkPrintOrderDetails().size());

    }
}
