
package uk.gov.hmcts.reform.prl.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.config.SendgridEmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.TransferCaseTemplate;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ATTACHMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NUMBER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISPOSITION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {
    public static final String PRL_RPA_NOTIFICATION = "Private Reform Law CCD Notification ";
    public static final String MAIL_SEND = "mail/send";
    public static final String CASE_NAME = "caseName";
    public static final String NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY = "Notification to party sent successfully";

    @Value("${send-grid.rpa.email.to}")
    private String toEmail;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    private final SendGrid sendGrid;
    private final DocumentGenService documentGenService;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;
    private final SendgridEmailTemplatesConfig sendgridEmailTemplatesConfig;

    public void sendEmail(JsonObject caseData) throws IOException {
        String subject = PRL_RPA_NOTIFICATION + caseData.get("id") + ".json";
        Content content = new Content("text/plain", " ");
        Attachments attachments = new Attachments();
        String data = Base64.getEncoder().encodeToString(caseData.toString().getBytes());
        attachments.setContent(data);
        attachments.setFilename(subject);
        attachments.setType("application/json");
        attachments.setDisposition("attachment");
        log.info("Initiating email to RPA through sendgrid from {}, to {}", fromEmail, toEmail);
        Mail mail = new Mail(new Email(fromEmail), subject, new Email(toEmail), content);
        mail.addAttachments(attachments);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            log.info("Initiating email through sendgrid");
            sendGrid.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public boolean sendEmailUsingTemplateWithAttachments(SendgridEmailTemplateNames sendgridEmailTemplateNames,
                                                      String authorization, SendgridEmailConfig sendgridEmailConfig) throws IOException {
        Personalization personalization = new Personalization();
        personalization.addTo(getEmail(sendgridEmailConfig.getToEmailAddress()));
        Map<String, Object> dynamicFields = sendgridEmailConfig.getDynamicTemplateData();
        if (MapUtils.isNotEmpty(dynamicFields)) {
            dynamicFields.forEach(personalization::addDynamicTemplateData);
        }
        Mail mail = new Mail();
        long attachDocsStartTime = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(sendgridEmailConfig.getListOfAttachments())) {
            attachFiles(authorization, mail, getCommonEmailProps(), sendgridEmailConfig.getListOfAttachments());
        }
        log.info("*** Time taken to attach docs to mail - {}s",
                 TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - attachDocsStartTime));
        mail.setFrom(getEmail(fromEmail));
        mail.addPersonalization(personalization);
        mail.setTemplateId(getTemplateId(sendgridEmailTemplateNames, sendgridEmailConfig.getLanguagePreference()));
        Request request = new Request();
        long startTime = System.currentTimeMillis();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            log.info("Sendgrid status code {}", response.getStatusCode());
            if (HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                log.info(NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY);
                return true;
            }
            return false;
        } catch (IOException ex) {
            log.info("Sendgrid exception is {}", ex.getMessage());
            throw new IOException(ex.getMessage());
        } finally {
            log.info("*** Response time taken by sendgrid - {}s",
                     TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
        }
    }

    private String getTemplateId(SendgridEmailTemplateNames templateName, LanguagePreference languagePreference) {
        return sendgridEmailTemplatesConfig.getTemplates().get(languagePreference).get(templateName);
    }


    private Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put(SUBJECT, "A case has been transferred to your court");
        emailProps.put(CONTENT, "Case details");
        emailProps.put(ATTACHMENT_TYPE, "pdf");
        emailProps.put(DISPOSITION, "attachment");
        return emailProps;
    }

    private Email getEmail(String toEmailAddress) {
        return new Email(toEmailAddress);
    }

    public void sendTransferCourtEmailWithAttachments(String authorization, Map<String, String> emailProps,
                                                             String toEmailAddress, List<Document> listOfAttachments)
        throws IOException {
        String subject = emailProps.get("subject");
        Content content = new Content("text/html", String.format(
            TransferCaseTemplate.TRANSFER_CASE_EMAIL_BODY,
            emailProps.get(CASE_NUMBER),
            emailProps.get(CASE_NAME),
            emailProps.get("issueDate"),
            emailProps.get("applicationType"),
            emailProps.get("confidentialityText"),
            emailProps.get("courtName")
        ));
        Mail mail = new Mail(new Email(fromEmail), subject, new Email(toEmailAddress), content);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }
        if (launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")) {
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint(MAIL_SEND);
                request.setBody(mail.build());
                Response response = sendGrid.api(request);
                if (HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                    log.info(NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY);
                }
            } catch (IOException ex) {
                log.error("Notification to parties failed");
                throw new IOException(ex.getMessage());
            }
        }
    }

    private void attachFiles(String authorization, Mail mail, Map<String,
        String> emailProps, List<Document> documents) {
        String s2sToken = authTokenGenerator.generate();
        documents.parallelStream().forEach(document -> {
            Attachments attachments = new Attachments();
            String documentAsString = Base64.getEncoder().encodeToString(documentGenService
                                                                      .getDocumentBytes(
                                                                          document.getDocumentUrl(),
                                                                          authorization,
                                                                          s2sToken
                                                                      ));
            attachments.setFilename(document.getDocumentFileName());
            attachments.setType(emailProps.get("attachmentType"));
            attachments.setDisposition(emailProps.get("disposition"));
            attachments.setContent(documentAsString);
            mail.addAttachments(attachments);
        });
    }

}
