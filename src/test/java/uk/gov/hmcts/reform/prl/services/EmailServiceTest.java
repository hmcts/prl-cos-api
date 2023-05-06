package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.CitizenEmailProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_EMAIL;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_PETITIONER_NAME;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_RESPONDENT_NAME;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    public static final String EMAIL_TEMPLATE_ID_1 = "111";
    public static final String EMAIL_TEMPLATE_ID_2 = "222";
    public static final CitizenEmail expectedEmailVars = CitizenEmailProvider.full();
    public static final ImmutableMap<String, String> expectedEmailVarsAsMap = ImmutableMap.of(
        "caseReference ", TEST_CASE_ID,
        "petitionerName", TEST_PETITIONER_NAME,
        "respondentName", TEST_RESPONDENT_NAME
    );

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private EmailService emailService;

    @Before
    public void setup() throws NotificationClientException {
        when(emailTemplatesConfig.getTemplates())
            .thenReturn(
                ImmutableMap.of(
                    LanguagePreference.english, ImmutableMap.of(EmailTemplateNames.EXAMPLE, EMAIL_TEMPLATE_ID_1),
                    LanguagePreference.welsh, ImmutableMap.of(EmailTemplateNames.EXAMPLE, EMAIL_TEMPLATE_ID_2)
                )
            );
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenReturn(mock(SendEmailResponse.class));
        when(objectMapper.convertValue(expectedEmailVars, Map.class)).thenReturn(expectedEmailVarsAsMap);
    }

    @Test
    public void sendShouldCallNotifyApi() throws NotificationClientException {
        emailService.send(
            TEST_EMAIL,
            EmailTemplateNames.EXAMPLE,
            expectedEmailVars,
            LanguagePreference.english
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
                TEST_EMAIL, EmailTemplateNames.EXAMPLE, expectedEmailVars, LanguagePreference.welsh
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
    public void shouldGetCaseData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.CASE_ISSUED.getValue())
            .build();
        CaseData caseData = CaseDetailsProvider.full().getCaseData();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CaseData caseData1 = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.valueOf(caseDetails.getState()))
            .build();
        assertEquals(emailService.getCaseData(caseDetails), caseData1);
    }
}
