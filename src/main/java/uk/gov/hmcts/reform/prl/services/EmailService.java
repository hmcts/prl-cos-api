package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;
    private final LaunchDarklyClient launchDarklyClient;

    public void send(String email,
                     EmailTemplateNames templateName,
                     EmailTemplateVars templateVars,
                     LanguagePreference languagePreference) {
        final String reference = templateVars.getCaseReference();
        onBeforeLog(templateName, templateVars.getCaseReference(), reference);
        final String templateId = getTemplateId(templateName, languagePreference);

        try {
            SendEmailResponse response = notificationClient.sendEmail(templateId, email, toMap(templateVars),
                                                                      reference);
            onAfterLog(templateName, templateVars.getCaseReference(), reference, response.getNotificationId());
        } catch (NotificationClientException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    //Added for Service of application, to enable and disable notifications to parties
    public void sendSoa(String email,
                     EmailTemplateNames templateName,
                     EmailTemplateVars templateVars,
                     LanguagePreference languagePreference) {
        final String reference = templateVars.getCaseReference();
        onBeforeLog(templateName, templateVars.getCaseReference(), reference);
        final String templateId = getTemplateId(templateName, languagePreference);

        try {
            if (launchDarklyClient.isFeatureEnabled("soa-gov-notify")) {
                SendEmailResponse response = notificationClient.sendEmail(templateId, email, toMap(templateVars),
                                                                          reference
                );
                onAfterLog(templateName, templateVars.getCaseReference(), reference, response.getNotificationId());
            }
        } catch (NotificationClientException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private void onAfterLog(EmailTemplateNames name, String caseId, String reference, UUID notificationId) {
        log.info(
            "CaseId: {}: email {} sent. NotificationId = {}, Reference = {}",
            caseId, name, notificationId, reference
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toMap(EmailTemplateVars templateVars) {
        return objectMapper.convertValue(templateVars, Map.class);
    }

    private void onBeforeLog(EmailTemplateNames name, String caseId, String reference) {
        log.info(
            "CaseId: {}: attempting to send email {} for Reference = {}",
            caseId, name, reference
        );
    }

    private String getTemplateId(EmailTemplateNames templateName, LanguagePreference languagePreference) {
        return emailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
    }

    protected CaseData getCaseData(CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.fromValue(caseDetails.getState()))
            .build();

    }
}
