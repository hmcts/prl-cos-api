package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
@RunWith(MockitoJUnitRunner.class)
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

    CaseData caseData;

    UserDetails userDetails;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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

        List<Court> courtList  = new ArrayList<>();
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

        when(courtFinderService.getClosestChildArrangementsCourt(caseData)).thenReturn(court);

        Assert.assertEquals(solicitorEmailService.buildEmail(caseDetails), email);

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

        when(courtFinderService.getClosestChildArrangementsCourt(caseData)).thenReturn(court);

        solicitorEmailService.sendEmail(caseDetails);
        assertEquals(caseDetails.getData().get("applicantSolicitorEmailAddress").toString(), "test@test.com");
    }


    @Test
    public void sendByPassEmailSuccessfully() throws NotFoundException {
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

        when(courtFinderService.getClosestChildArrangementsCourt(caseData)).thenReturn(court);

        solicitorEmailService.sendEmailBypss(caseDetails, authToken);
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

        String email = (!applicantList.isEmpty() && applicantList.get(0).getEmail() != null) ? String.valueOf(applicantList.get(0).getEmail())
            : userDetails.getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("applicantSolicitorEmailAddress", email);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(data)
            .build();

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        solicitorEmailService.sendEmailToSolicitor(caseDetails, userDetails);
        assertEquals("test@demo.com", caseDetails.getData().get("applicantSolicitorEmailAddress").toString());
    }
}

