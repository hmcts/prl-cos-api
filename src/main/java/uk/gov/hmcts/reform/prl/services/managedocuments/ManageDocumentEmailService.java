package uk.gov.hmcts.reform.prl.services.managedocuments;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.SendgridEmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.services.SendgridService.MAIL_SEND;
import static uk.gov.hmcts.reform.prl.services.SendgridService.NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentEmailService {

    private final SendgridEmailTemplatesConfig sendgridEmailTemplatesConfig;

    private final SendGrid sendGrid;
    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;


    public void sendEmailUsingTemplateWithAttachments(SendgridEmailTemplateNames sendgridEmailTemplateNames,
                                                       SendgridEmailConfig sendgridEmailConfig) throws IOException {
        Personalization personalization = new Personalization();
        personalization.addTo(getEmail(sendgridEmailConfig.getToEmailAddress()));
        Map<String, Object> dynamicFields = sendgridEmailConfig.getDynamicTemplateData();
        if (MapUtils.isNotEmpty(dynamicFields)) {
            dynamicFields.forEach(personalization::addDynamicTemplateData);
        }
        Mail mail = new Mail();
        mail.setFrom(getEmail(fromEmail));
        mail.addPersonalization(personalization);
        mail.setTemplateId(getTemplateId(sendgridEmailTemplateNames, sendgridEmailConfig.getLanguagePreference()));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("Sendgrid status code {}", response.getStatusCode());
            if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                log.info(NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY);
            }
        } catch (IOException ex) {
            log.info("error is {}", ex.getMessage());
            throw new IOException(ex.getMessage());
        }
    }

    private Email getEmail(String toEmailAddress) {
        return new Email(toEmailAddress);
    }

    private String getTemplateId(SendgridEmailTemplateNames templateName, LanguagePreference languagePreference) {
        return sendgridEmailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
    }


}
