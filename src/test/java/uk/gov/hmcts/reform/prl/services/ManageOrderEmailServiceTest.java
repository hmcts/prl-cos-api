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
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

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
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .build();

        List<Court> courtList = new ArrayList<>();
        courtList.add(court);
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

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .cafcassCymruServedOptions(YesOrNo.Yes)
            .cafcassServedOptions(YesOrNo.Yes)
            .serveToRespondentOptions(YesOrNo.No)
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .value(List.of(DynamicMultiselectListElement.builder()
                                                      .label("John (Child 1)")
                                                      .code("00000000-0000-0000-0000-000000000000")
                                                      .build())).build())
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

        manageOrderEmailService.sendEmailWhenOrderIsServed(caseDetails);
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
                                                         .build()).build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        manageOrderEmailService.sendEmailWhenOrderIsServed(CaseDetails.builder().build());

        Mockito.verify(emailService,Mockito.times(4)).send(Mockito.anyString(),
                                                           Mockito.any(),
                                                           Mockito.any(),Mockito.any());
    }

    @Test
    public void testSendEmailWhenOrderServedShouldInvoke() {
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
                              .cafcassEmailId("test").build())
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        manageOrderEmailService.sendEmailWhenOrderIsServed(CaseDetails.builder().build());

        Mockito.verify(emailService,Mockito.times(1)).getCaseData(Mockito.any());
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
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);
        manageOrderEmailService.sendEmailWhenOrderIsServed(CaseDetails.builder().build());
        Mockito.verify(emailService,Mockito.times(5)).getCaseData(Mockito.any());
    }
}
