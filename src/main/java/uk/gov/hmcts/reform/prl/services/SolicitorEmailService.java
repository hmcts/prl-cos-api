package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.EmailObfuscator;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorEmailService {

    @Value("${xui.url}")
    String manageCaseUrl;

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;
    public void buildAndSendEmail (CaseDetails caseDetails, UserDetails userDetails){


        List<PartyDetails> applicants = caseDetails.getCaseData()
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element-> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        String applicantNames = String.join(", ", applicantNamesList);

        EmailTemplateVars emailTemplateVars = SolicitorEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName("court name")
            .fullName(userDetails.getFullName())
            .courtEmail("C100applications@justice.gov.uk")
            .caseLink(manageCaseUrl + caseDetails.getCaseId())
            .build();

        send(
            getRecipientEmail(userDetails),
            EmailTemplateNames.EXAMPLE,
            emailTemplateVars,
            LanguagePreference.ENGLISH,
            caseDetails.getCaseId()
        );

    }
    public String getRecipientEmail(UserDetails userDetails) {

        return userDetails.getEmail() != null ? userDetails.getEmail() : "prl_caseworker_solicitor@mailinator.com";

    }
    public void send(String email,
                     EmailTemplateNames templateName,
                     EmailTemplateVars templateVars,
                     LanguagePreference languagePreference,
                     String caseReference) {

        final String reference = caseReference;

        onBeforeLog(email, templateName, templateVars.getCaseReference(), reference);

        final String templateId = getTemplateId(templateName, languagePreference);

        try {

            SendEmailResponse response = notificationClient.sendEmail(templateId, email, toMap(templateVars),
                                                                      reference);
            onAfterLog(templateName, templateVars.getCaseReference(), reference, response.getNotificationId());

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

    private void onBeforeLog(String email, EmailTemplateNames name, String caseId, String reference) {
        log.info(
            "CaseId: {}: attempting to send email {} to {}. Reference = {}",
            caseId, name, EmailObfuscator.obfuscate(email), reference
        );
    }

    private String generateReference() {
        return UUID.randomUUID().toString();
    }

    private String getTemplateId(EmailTemplateNames templateName, LanguagePreference languagePreference) {
        return emailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
    }
}
