package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CitizenEmailProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.*;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorEmailServiceTest {

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
        .build();;


    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private SolicitorEmailService emailService;

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
        Map<String, String> expectedEmailVarsAsMap = new HashMap<>();

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
            LanguagePreference.ENGLISH,"123"
        );
        Map<String, String> expectedEmailVarsAsMap = new HashMap<>();

        expectedEmailVarsAsMap.put("caseReference", "123");
        expectedEmailVarsAsMap.put("caseName", "Case 123");
        expectedEmailVarsAsMap.put("applicantName", "applicantName");
        expectedEmailVarsAsMap.put("courtName", "court name");
        expectedEmailVarsAsMap.put("fullName", "Full name");
        expectedEmailVarsAsMap.put("courtEmail", "C@justice.gov.uk");
        expectedEmailVarsAsMap.put("caseLink", "http://localhost:3333/");

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
        Map<String, String> expectedEmailVarsAsMap = new HashMap<>();

        expectedEmailVarsAsMap.put("caseReference", "123");
        expectedEmailVarsAsMap.put("caseName", "Case 123");
        expectedEmailVarsAsMap.put("applicantName", "applicantName");
        expectedEmailVarsAsMap.put("courtName", "court name");
        expectedEmailVarsAsMap.put("fullName", "Full name");
        expectedEmailVarsAsMap.put("courtEmail", "C@justice.gov.uk");
        expectedEmailVarsAsMap.put("caseLink", "http://localhost:3333/");

        assertThrows(
            IllegalArgumentException.class,
            () -> emailService.send(
                TEST_EMAIL, EmailTemplateNames.EXAMPLE, expectedEmailVars, LanguagePreference.WELSH,"123"
            )
        );

        verify(notificationClient).sendEmail(
            eq(EMAIL_TEMPLATE_ID_2),
            eq(TEST_EMAIL),
            eq(expectedEmailVarsAsMap),
            anyString()
        );
    }
}
