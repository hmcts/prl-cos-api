package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorEmailServiceTest {

    @Value("${xui.url}")
    String manageCaseUrl;

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
    private NotificationClient notificationClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @InjectMocks
    private EmailService emailService;

    @Mock
    UserService userService;

    private Map<String, String> expectedEmailVarsAsMap;

    @Before
    public void setup() throws NotificationClientException {
        when(emailTemplatesConfig.getTemplates())
            .thenReturn(
                ImmutableMap.of(
                    LanguagePreference.ENGLISH, ImmutableMap.of(EmailTemplateNames.EXAMPLE, EMAIL_TEMPLATE_ID_1),
                    LanguagePreference.WELSH, ImmutableMap.of(EmailTemplateNames.EXAMPLE, EMAIL_TEMPLATE_ID_2)
                )
            );
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenReturn(mock(SendEmailResponse.class));
        expectedEmailVarsAsMap = new HashMap<>();

        expectedEmailVarsAsMap.put("caseReference", "123");
        expectedEmailVarsAsMap.put("caseName", "Case 123");
        expectedEmailVarsAsMap.put("applicantName", "applicantName");
        expectedEmailVarsAsMap.put("courtName", "court name");
        expectedEmailVarsAsMap.put("fullName", "Full name");
        expectedEmailVarsAsMap.put("courtEmail", "C@justice.gov.uk");
        expectedEmailVarsAsMap.put("caseLink", "http://localhost:3333/");
        when(objectMapper.convertValue(expectedEmailVars, Map.class)).thenReturn(expectedEmailVarsAsMap);
    }

    @Test
    public void sendShouldCallNotifyApi() throws NotificationClientException {
        emailService.send(
            TEST_EMAIL,
            EmailTemplateNames.EXAMPLE,
            expectedEmailVars,
            LanguagePreference.ENGLISH
        );

        verify(notificationClient).sendEmail(
            eq(EMAIL_TEMPLATE_ID_1),
            eq(TEST_EMAIL),
            eq(expectedEmailVarsAsMap),
            anyString()
        );
    }

    @Test
    public void sendShouldHandleNotificationClientExceptionAndRethrow() throws NotificationClientException {
        when(notificationClient.sendEmail(eq(EMAIL_TEMPLATE_ID_2), any(), any(), any()))
            .thenThrow(NotificationClientException.class);

        assertThrows(
            IllegalArgumentException.class,
            () -> emailService.send(
                TEST_EMAIL, EmailTemplateNames.EXAMPLE, expectedEmailVars, LanguagePreference.WELSH
            )
        );

        verify(notificationClient).sendEmail(
            eq(EMAIL_TEMPLATE_ID_2),
            eq(TEST_EMAIL),
            eq(expectedEmailVarsAsMap),
            anyString()
        );
    }

    @Test
    public void whenUserDetailsProvidedThenValidEmailReturned() {

        when(userService.getUserDetails("Auth")).thenReturn(UserDetails.builder()
                                                                .email("test@email.com")
                                                                .build());

        UserDetails userDetails = userService.getUserDetails("Auth");

        when(solicitorEmailService.getRecipientEmail(userDetails)).thenReturn("test@email.com");

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
            .courtName("court name")
            .fullName(userDetails.getFullName())
            .courtEmail("C100applications@justice.gov.uk")
            .caseLink(manageCaseUrl + caseDetails.getCaseId())
                .build();

        when(solicitorEmailService.buildEmail(caseDetails, userDetails)).thenReturn(email);

        assertEquals(solicitorEmailService.buildEmail(caseDetails, userDetails), email);

    }

}

