package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorEmailServiceTest {


    private static final String manageCaseUrl = null;
    public static final String EMAIL_TEMPLATE_ID_1 = "111";
    public static final String EMAIL_TEMPLATE_ID_2 = "222";
    public static final SolicitorEmail expectedEmailVars = SolicitorEmail.builder()
        .caseReference("123")
        .caseName("Case 123")
        .applicantName("applicantName")
        .courtName("court name")
        .fullName("Full name")
        .courtEmail("C@justice.gov.uk")
        .caseLink("http://localhost:3333/")
        .build();


    @Mock
    private EmailService emailService;

    @Mock
    UserService userService;

    @InjectMocks
    private SolicitorEmailService solicitorEmailService;

    private Map<String, String> expectedEmailVarsAsMap;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void whenApplicantPresentThenApplicantStringCreated() {

        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        String applicantNames = "TestFirst TestLast";

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
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
            .caseLink(manageCaseUrl + caseDetails.getId())
            .build();

        assertEquals(solicitorEmailService.buildEmail(caseDetails), email);

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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
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

        when(emailService.getCaseData(caseDetails)).thenReturn(caseData);

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .caseLink(manageCaseUrl + caseDetails.getId())
            .build();

        solicitorEmailService.sendEmail(caseDetails);
        assertEquals(caseDetails.getData().get("applicantSolicitorEmailAddress").toString(), "test@test.com");
    }

}

