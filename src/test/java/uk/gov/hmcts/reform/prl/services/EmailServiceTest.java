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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.CitizenEmailProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
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

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Value("${citizen.url}")
    private String citizenUrl;

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

    @Test
    public void testToSendSoaEmail() throws NotificationClientException {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                            .canYouProvideEmailAddress(YesOrNo.Yes)
                                            .email("test@applicant.com")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))

            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.CASE_ISSUED.getValue())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CaseData caseData1 = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.valueOf(caseDetails.getState()))
            .build();
        when(launchDarklyClient.isFeatureEnabled("soa-gov-notify")).thenReturn(true);
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenReturn(mock(SendEmailResponse.class));
        emailService.sendSoa("test@applicant.com", EmailTemplateNames.CA_APPLICANT_SERVICE_APPLICATION,
                                          emailTemplateVars, LanguagePreference.english);

        verify(launchDarklyClient, times(1)).isFeatureEnabled("soa-gov-notify");

    }

}
