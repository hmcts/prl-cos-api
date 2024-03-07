package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.GatekeeperEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerEmailServiceTest {

    private static final String manageCaseUrl = null;
    public static final String EMAIL_TEMPLATE_ID_1 = "111";
    public static final String EMAIL_TEMPLATE_ID_2 = "222";
    private static final String URGENT_CASE = "Urgent Case";
    private static final String WITHOUT_NOTICE = "Without Notice";
    private static final String STANDARAD_HEARING = "Standard Hearing";

    public static final CaseWorkerEmail expectedEmailVars = CaseWorkerEmail.builder()
        .caseReference("123")
        .caseName("Case 123")
        .applicantName("applicantName")
        .respondentLastName("respondentName")
        .hearingDateRequested("  ")
        .ordersApplyingFor("CA Order")
        .typeOfHearing("Urgent")
        .courtEmail("C@justice.gov.uk")
        .caseLink("http://localhost:3333/")
        .build();


    @Mock
    private EmailService emailService;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private CaseWorkerEmailService caseWorkerEmailService;

    private PartyDetails applicant;
    private PartyDetails respondent;
    private String applicantNames;
    private Element<PartyDetails> wrappedApplicants;
    private List<Element<PartyDetails>> listOfApplicants;

    private Element<PartyDetails> wrappedRespondents;
    private List<Element<PartyDetails>> listOfRespondents;


    @Before
    public void setUp() {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        applicantNames = "TestFirst TestLast";

        wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        listOfApplicants = Collections.singletonList(wrappedApplicants);

        respondent = PartyDetails.builder()
            .lastName("TestLast")
            .build();

        wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        listOfRespondents = Collections.singletonList(wrappedRespondents);

    }

    @Test
    public void whenApplicantPresentThenApplicantStringCreated() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .isCaseUrgent(YesOrNo.Yes)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondent.getLastName())
            .typeOfHearing("Urgent ")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Child Arrangements Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();


        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildEmail(caseDetails));

    }

    @Test
    public void whenRespondentPresentThenRespondentStringCreated() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.prohibitedStepsOrder))
            .isCaseUrgent(YesOrNo.No)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondent.getLastName())
            .typeOfHearing("Standard hearing")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Prohibited Steps Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildEmail(caseDetails));


    }

    @Test
    public void whenTypeOfApplicationPresentThenOrdersApplyForWillBeDispalyed() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.specificIssueOrder))
            .isCaseUrgent(YesOrNo.No)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.Yes)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondent.getLastName())
            .typeOfHearing("Without notice")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Specific Issue Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildEmail(caseDetails));

    }

    @Test
    public void whenTypeOfApplicationIsReducedNoticeThenOrdersApplyForWillBeDispalyed() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.specificIssueOrder))
            .isCaseUrgent(YesOrNo.No)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
            .doYouRequireAHearingWithReducedNotice(YesOrNo.Yes)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondent.getLastName())
            .typeOfHearing("Reduced notice")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Specific Issue Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildEmail(caseDetails));

    }

    @Test
    public void sendEmailSuccessfully() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        PartyDetails respondent = PartyDetails.builder()
            .lastName("respondentLast")
            .build();

        String respondentNames = "TestLast";

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.specificIssueOrder))
            .isCaseUrgent(YesOrNo.No)
            .doYouNeedAWithoutNoticeHearing(YesOrNo.No)
            .caseworkerEmailAddress("test@test.com")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.Yes)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("caseworkerEmailAddress", "test@test.com");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName("respondentLast")
            .typeOfHearing("Reduced notice")
            .hearingDateRequested("  ")
            .ordersApplyingFor("Specific Issue Order")
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        caseWorkerEmailService.sendEmail(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("caseworkerEmailAddress").toString());
    }

    @Test
    public void testCourtAdminEmailWithNoUrgency() {

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

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency("")
            .isCaseUrgent("No")
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildCourtAdminEmail(caseDetails));

    }

    @Test
    public void testCourtAdminEmailWithUrgencyAndConfidentialInfo() {

        PartyDetails applicant1 = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.Yes)
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
            .isCaseUrgent(YesOrNo.Yes)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency("Urgent ")
            .isCaseUrgent("Yes")
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildCourtAdminEmail(caseDetails));

    }

    @Test
    public void testSendEmailToCourtAdmin() {

        LocalCourtAdminEmail localCourtAdminEmail = LocalCourtAdminEmail.builder()
            .email("test@demo.com")
            .build();

        Element<LocalCourtAdminEmail> wrappedEmail = Element.<LocalCourtAdminEmail>builder().value(localCourtAdminEmail).build();
        List<Element<LocalCourtAdminEmail>> emailList = Collections.singletonList(wrappedEmail);
        PartyDetails applicant1 = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant1).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Child child = Child.builder()
            .isChildAddressConfidential(YesOrNo.Yes)
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
            .localCourtAdmin(emailList)
            .isCaseUrgent(YesOrNo.Yes)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        caseWorkerEmailService.sendEmailToCourtAdmin(caseDetails);

        assertEquals(emailList, caseData.getLocalCourtAdmin());
    }

    @Test
    public void sendReturnApplicationEmailSuccessfully() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("TestFirst2")
            .lastName("TestLast3")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> listOfApplicants = new ArrayList<>();
        listOfApplicants.add(wrappedApplicants);
        listOfApplicants.add(wrappedApplicant2);


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .rejectReason(Collections.singletonList(RejectReasonEnum.consentOrderNotProvided))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");


        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseName(caseData.getApplicantCaseName())
            .contentFromDev(caseData.getReturnMessage())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        caseWorkerEmailService.sendReturnApplicationEmailToSolicitor(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());

    }

    @Test
    public void sendFl401ReturnApplicationEmailSuccessfully() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(applicant)
            .fl401RejectReason(Collections.singletonList(FL401RejectReasonEnum.consentOrderNotProvided))
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", "test@test.com");


        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();
        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseName(caseData.getApplicantCaseName())
            .contentFromDev(caseData.getReturnMessage())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        caseWorkerEmailService.sendReturnApplicationEmailToSolicitor(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());

    }

    @Test
    public void testGateKeeperEmailWithNoUrgency() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .isCaseUrgent(YesOrNo.No)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency("")
            .isCaseUrgent("No")
            .issueDate(issueDate.format(dateTimeFormatter))
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildGatekeeperEmail(caseDetails));

    }

    @Test
    public void testGateKeeperEmailWithUrgency() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .isCaseUrgent(YesOrNo.Yes)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency("Urgent ")
            .isCaseUrgent("Yes")
            .issueDate(issueDate.format(dateTimeFormatter))
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildGatekeeperEmail(caseDetails));

    }

    @Test
    public void testSendEmailToGateKeeper() {

        GatekeeperEmail gatekeeperEmail = GatekeeperEmail.builder()
            .email("test@demo.com")
            .build();

        Element<GatekeeperEmail> wrappedEmail = Element.<GatekeeperEmail>builder().value(gatekeeperEmail).build();
        List<Element<GatekeeperEmail>> emailList = Collections.singletonList(wrappedEmail);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .gatekeeper(emailList)
            .isCaseUrgent(YesOrNo.Yes)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        caseWorkerEmailService.sendEmailToGateKeeper(caseDetails);

        assertEquals(emailList, caseData.getGatekeeper());
    }

    @Test
    public void testSendEmailToGateKeeperFl401() {

        GatekeeperEmail gatekeeperEmail = GatekeeperEmail.builder()
            .email("test@demo.com")
            .build();

        Element<GatekeeperEmail> wrappedEmail = Element.<GatekeeperEmail>builder().value(gatekeeperEmail).build();
        List<Element<GatekeeperEmail>> emailList = Collections.singletonList(wrappedEmail);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("TestCaseName")
            .gatekeeper(emailList)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        caseWorkerEmailService.sendEmailToGateKeeper(caseDetails);

        assertEquals(emailList, caseData.getGatekeeper());
    }

    @Test
    public void testFL401LocalCourtEmailWithoutConfidentialInformation() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@courtadmin.com")
            .canYouProvideEmailAddress(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String isConfidential = "No";
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (null != fl401Applicant.getIsEmailAddressConfidential()
            && YesOrNo.Yes.equals(fl401Applicant.getIsEmailAddressConfidential()))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = "Yes";
        }

        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildFl401LocalCourtAdminEmail(caseDetails));

    }

    @Test
    public void testFL401LocalCourtEmailWithConfidentialInformation() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@courtadmin.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String isConfidential = "No";
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (fl401Applicant.getIsEmailAddressConfidential() != null
            && fl401Applicant.getIsEmailAddressConfidential().equals(YesOrNo.Yes))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = "Yes";
        }
        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, caseWorkerEmailService.buildFl401LocalCourtAdminEmail(caseDetails));

    }

    @Test
    public void testSendEmailToFl401LocalCourt() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@courtadmin.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("testing@localcourt.com")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String isConfidential = "No";
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (fl401Applicant.getIsEmailAddressConfidential() != null
            && fl401Applicant.getIsEmailAddressConfidential().equals(YesOrNo.Yes))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = "Yes";
        }
        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());

        assertEquals("testing@localcourt.com", caseData.getCourtEmailAddress());
    }

    @Test
    public void testSendEmailToFl401LocalCourtWhenWithdrawnAfterIssued() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@courtadmin.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .issueDate(LocalDate.now())
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("testing@localcourt.com")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String isConfidential = "No";
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (fl401Applicant.getIsEmailAddressConfidential() != null
            && fl401Applicant.getIsEmailAddressConfidential().equals(YesOrNo.Yes))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = "Yes";
        }
        EmailTemplateVars email = CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        caseWorkerEmailService.sendWithdrawApplicationEmailToLocalCourt(caseDetails, caseData.getCourtEmailAddress());

        assertEquals("testing@localcourt.com", caseData.getCourtEmailAddress());
    }
}

