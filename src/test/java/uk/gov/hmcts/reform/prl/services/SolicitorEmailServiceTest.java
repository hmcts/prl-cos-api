package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Assert;
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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class SolicitorEmailServiceTest {


    private static final String manageCaseUrl = null;
    public static final String authToken = "Bearer TestAuthToken";

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private CourtFinderService courtFinderService;

    @InjectMocks
    private SolicitorEmailService solicitorEmailService;

    private Map<String, String> expectedEmailVarsAsMap;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private Court court;

    @Mock
    private ServiceArea serviceArea;
    @Mock
    private ObjectMapper objectMapper;

    CaseData caseData;

    UserDetails userDetails;

    @Before
    public void setUp() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .build();

        List<Court> courtList = new ArrayList<>();
        courtList.add(court);

        serviceArea = ServiceArea.builder()
            .courts(courtList)
            .build();

        userDetails = UserDetails.builder()
            .email("solicitor@example.com")
            .surname("userLast")
            .build();
    }

    @Test
    public void whenApplicantPresentThenApplicantStringCreated() throws NotFoundException {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

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
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);

        Assert.assertEquals(solicitorEmailService.buildEmail(caseDetails, false), email);

    }

    @Test
    public void testIsApplicantPayingC100() throws NotFoundException {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

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
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId() + "#Service%20Request")
            .build();

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);

        Assert.assertEquals(solicitorEmailService.buildEmail(caseDetails, true), email);

    }

    @Test
    public void sendEmailSuccessfully() throws NotFoundException {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

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

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);

        solicitorEmailService.sendEmail(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testSendEmailForWithdraw() throws NotFoundException {

        List<PartyDetails> applicantList = new ArrayList<>();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorEmail("test@demo.com")
            .build();

        applicantList.add(applicant);

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@demo.com")
            .applicants(listOfApplicants)
            .courtName("testcourt")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("test@demo.com")
            .build();

        String email = (!applicantList.isEmpty() && applicantList.get(0).getEmail() != null) ? String.valueOf(
            applicantList.get(0).getEmail())
            : userDetails.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", email);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToSolicitor(caseDetails, userDetails);
        assertEquals("test@demo.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testSendEmailForWithdrawWhenApplicantSolicitorEmailNotPresent() throws NotFoundException {

        List<PartyDetails> applicantList = new ArrayList<>();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        applicantList.add(applicant);

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@demo.com")
            .applicants(listOfApplicants)
            .courtName("testcourt")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("test@demo.com")
            .build();

        String email = (!applicantList.isEmpty() && applicantList.get(0).getEmail() != null) ? String.valueOf(
            applicantList.get(0).getEmail())
            : userDetails.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", email);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToSolicitor(caseDetails, userDetails);
        assertEquals("test@demo.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testFL401SolicitorEmail() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@solicitor.com")
            .build();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .build();

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantFullName)
            .courtName(caseData.getCourtName())
            .courtEmail(caseData.getCourtEmailAddress())
            .caseLink(manageCaseUrl + "/" + caseData.getId())
            .build();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        assertEquals(email, solicitorEmailService.buildFl401SolicitorEmail(caseDetails));
    }

    @Test
    public void testSendEmailToFl401LocalCourt() {

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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("testing@solicitor.com")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", caseData.getCourtEmailAddress());
    }

    @Test
    public void testSendApplicationSubmittedEmailToFl401SolicitorFromUserDetails() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .build();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendEmailToFl401Solicitor(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", email);
    }

    @Test
    public void testSendWithdrawEmailToFl401Solicitor() {

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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("testing@solicitor.com")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToFl401Solicitor(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", email);
    }

    @Test
    public void testSendWithdrawEmailToFl401SolicitorFromUserDetails() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .build();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToFl401Solicitor(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", email);
    }

    @Test
    public void testSendEmailForWithdrawAfterIssued() throws NotFoundException {

        List<PartyDetails> applicantList = new ArrayList<>();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorEmail("test@demo.com")
            .build();

        applicantList.add(applicant);

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@demo.com")
            .applicants(listOfApplicants)
            .courtName("testcourt")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("test@demo.com")
            .build();

        String email = (!applicantList.isEmpty() && applicantList.get(0).getEmail() != null) ? String.valueOf(
            applicantList.get(0).getEmail())
            : userDetails.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", email);
        data.put("issueDate", "12/12/1212");
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToSolicitorAfterIssuedState(caseDetails, userDetails);
        assertEquals("test@demo.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testSendEmailForWithdrawAfterIssuedWithEmptySolicitorEmail() throws NotFoundException {

        List<PartyDetails> applicantList = new ArrayList<>();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .solicitorEmail("")
            .build();

        applicantList.add(applicant);

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@demo.com")
            .applicants(listOfApplicants)
            .courtName("testcourt")
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("test@demo.com")
            .build();

        String email = (!applicantList.isEmpty() && applicantList.get(0).getEmail() != null) ? String.valueOf(
            applicantList.get(0).getEmail())
            : userDetails.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", email);
        data.put("issueDate", "12/12/1212");
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToSolicitorAfterIssuedState(caseDetails, userDetails);
        assertEquals("test@demo.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void testSendWithdrawEmailToFl401SolicitorAfterIssued() {
        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .build();
        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        data.put("issueDate", "12/12/1212");
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToFl401SolicitorAfterIssuedState(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", email);
    }

    @Test
    public void testSendWithdrawEmailToFl401SolicitorAfterIssuedWithoutEmail() {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .build();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();
        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .email("testing@solicitor.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("testing@solicitor.com")
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", fl401Applicant.getSolicitorEmail());
        data.put("issueDate", "12/12/1212");
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        String email = fl401Applicant.getSolicitorEmail() != null ? fl401Applicant.getSolicitorEmail() : userDetails.getEmail();

        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);

        solicitorEmailService.sendWithDrawEmailToFl401SolicitorAfterIssuedState(caseDetails, userDetails);

        assertEquals("testing@solicitor.com", email);
    }

    @Test
    public void sendReSubmitEmailSuccessfully() throws NotFoundException {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

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

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(court.getCourtName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);

        solicitorEmailService.sendReSubmitEmail(caseDetails);
        assertEquals("test@test.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }

    @Test
    public void sendAwaitingPaymentEmailSuccessfully() throws NotFoundException {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails caseDetails = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .state("PENDING").caseId("123").caseData(
            CaseData.builder()
                .id(12345L)
                .applicantCaseName("TestCaseName")
                .applicants(listOfApplicants)
                .children(listOfChildren)
                .courtName("testcourt")
                .applicantSolicitorEmailAddress("hello@gmail.com").build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder().state(caseDetails.getState())
            .id(Long.valueOf(caseDetails.getCaseId()))
            .data(caseDetails.getCaseData()
                      .toMap(objectMapper)).build();
        when(emailService.getCaseData(caseDetails1)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        solicitorEmailService.sendAwaitingPaymentEmail(caseDetails);
        assertEquals("hello@gmail.com", caseDetails.getCaseData().getApplicantSolicitorEmailAddress());
    }

    @Test
    public void testHelpWithFeesEmail() throws NotFoundException {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails caseDetails = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .state("PENDING").caseId("123").caseData(
                CaseData.builder()
                    .id(12345L)
                    .applicantCaseName("TestCaseName")
                    .applicants(listOfApplicants)
                    .children(listOfChildren)
                    .courtName("testcourt")
                    .applicantSolicitorEmailAddress("hello@gmail.com").build()).build();
        CaseDetails caseDetails1 = CaseDetails.builder().state(caseDetails.getState())
            .id(Long.valueOf(caseDetails.getCaseId()))
            .data(caseDetails.getCaseData()
                      .toMap(objectMapper)).build();
        when(emailService.getCaseData(caseDetails1)).thenReturn(caseData);
        when(courtFinderService.getNearestFamilyCourt(caseData)).thenReturn(court);
        solicitorEmailService.sendHelpWithFeesEmail(caseDetails);
        assertEquals("hello@gmail.com", caseDetails.getCaseData().getApplicantSolicitorEmailAddress());
    }
}

