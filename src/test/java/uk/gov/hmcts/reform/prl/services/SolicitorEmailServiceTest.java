package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorEmailServiceTest {


    private static final String manageCaseUrl = null;
    public static final String EMAIL_TEMPLATE_ID_1 = "111";
    public static final String EMAIL_TEMPLATE_ID_2 = "222";
    public static final SolicitorEmail expectedEmailVars =  SolicitorEmail.builder()
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

    @Test
    public void whenUserDetailsProvidedThenValidEmailReturned() {

        when(userService.getUserDetails("Auth")).thenReturn(UserDetails.builder()
                                                                .email("test@email.com")
                                                                .build());

        UserDetails userDetails = userService.getUserDetails("Auth");

        assertEquals(solicitorEmailService.getRecipientEmail(userDetails), "test@email.com");
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
            .applicantCaseName("TestCaseName")
            .applicants(listOfApplicants)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        UserDetails userDetails = UserDetails.builder().build();

        EmailTemplateVars email = SolicitorEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .fullName(userDetails.getFullName())
            .caseLink(manageCaseUrl + caseDetails.getCaseId())
                .build();

        assertEquals(solicitorEmailService.buildEmail(caseDetails, userDetails), email);

    }

    @Test
    public void testGetRecipientDetails() {

        UserDetails userDetails = UserDetails.builder()
            .email("test@email.com")
            .build();

        String expected = "test@email.com";

        assertEquals(solicitorEmailService.getRecipientEmail(userDetails), expected);
    }

}

