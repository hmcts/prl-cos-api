
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.config.SendgridEmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.TransferCaseTemplate;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.EMAIL_END;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.EMAIL_START;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.FINAL_ORDER_TITLE;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.NEW_ORDER_TITLE;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RESPONDENT_SOLICITOR_FINAL_ORDER_EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RESPONDENT_SOLICITOR_SERVE_ORDER_EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.SPECIAL_INSTRUCTIONS_EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ATTACHMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NUMBER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISPOSITION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {
    public static final String PRL_RPA_NOTIFICATION = "Private Reform Law CCD Notification ";
    public static final String MAIL_SEND = "mail/send";
    public static final String CASE_NAME = "caseName";
    public static final String NOTIFICATION_TO_PARTY_SENT_SUCCESSFULLY = "Notification to party sent successfully";
    @Value("${send-grid.api-key}")
    private String apiKey;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${send-grid.rpa.email.to}")
    private String toEmail;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    @Value("${send-grid.notification.emailId.from}")
    private String fromEmailSendgrid;

    private final SendGrid sendGrid;
    private final DocumentGenService documentGenService;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;
    private final SendgridEmailTemplatesConfig sendgridEmailTemplatesConfig;
    private final ResourceLoader resourceLoader;

    public void sendEmail(JsonObject caseData) throws IOException {
        String subject = PRL_RPA_NOTIFICATION + caseData.get("id") + ".json";
        Content content = new Content("text/plain", " ");
        Attachments attachments = new Attachments();
        String data = Base64.getEncoder().encodeToString(caseData.toString().getBytes());
        attachments.setContent(data);
        attachments.setFilename(subject);
        attachments.setType("application/json");
        attachments.setDisposition("attachment");
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

    public void sendEmailUsingTemplateWithAttachments(SendgridEmailTemplateNames sendgridEmailTemplateNames,
                                                      String authorization, SendgridEmailConfig sendgridEmailConfig) throws IOException {
        Personalization personalization = new Personalization();
        personalization.addTo(getEmail(sendgridEmailConfig.getToEmailAddress()));
        Map<String, Object> dynamicFields = sendgridEmailConfig.getDynamicTemplateData();
        if (MapUtils.isNotEmpty(dynamicFields)) {
            dynamicFields.forEach(personalization::addDynamicTemplateData);
        }
        Mail mail = new Mail();
        if (CollectionUtils.isNotEmpty(sendgridEmailConfig.getListOfAttachments())) {
            attachFiles(authorization, mail, getCommonEmailProps(), sendgridEmailConfig.getListOfAttachments());
        }
        log.info("from email sendgrid {} ", fromEmailSendgrid);
        mail.setFrom(getEmail(fromEmailSendgrid));
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

    public EmailNotificationDetails sendEmailWithAttachments(String authorization, Map<String, String> emailProps,
                                                             String toEmailAddress, List<Document> listOfAttachments, String servedParty)
        throws IOException {

        Content content;
        String subject = emailProps.get("subject");
        if (emailProps.containsKey("orderURLLinkNeeded")) {
            subject = emailProps.get("orderSubject");
            emailProps.put("orderUrLLink", manageCaseUrl + URL_STRING + emailProps.get(CASE_NUMBER) + "#Orders");
            String title = emailProps.containsKey("finalOrder") ? FINAL_ORDER_TITLE : NEW_ORDER_TITLE;
            String body = emailProps.containsKey("finalOrder")
                ? RESPONDENT_SOLICITOR_FINAL_ORDER_EMAIL_BODY : RESPONDENT_SOLICITOR_SERVE_ORDER_EMAIL_BODY;

            String emailStart = String.format(
                EMAIL_START,
                emailProps.get(CASE_NAME),
                emailProps.get("caseNumber"),
                emailProps.get("solicitorName")
            );

            String emailEnd = String.format(
                EMAIL_END,
                emailProps.get("orderUrLLink")
            );

            content = new Content("text/html", String.format("%s%s%s%s", title, emailStart, body, emailEnd));
        } else {
            content = new Content("text/plain", String.format(
                    (emailProps.containsKey("specialNote") && emailProps.get("specialNote")
                            .equalsIgnoreCase("Yes")) ? SPECIAL_INSTRUCTIONS_EMAIL_BODY : EMAIL_BODY,
                    emailProps.get(CASE_NAME),
                    emailProps.get(CASE_NUMBER),
                    emailProps.get("solicitorName")
            ));
        }
        log.info("from email sendgrid {} ", fromEmailSendgrid);
        Mail mail = new Mail(new Email(fromEmailSendgrid), subject + emailProps.get(CASE_NAME), new Email(toEmailAddress), content);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }

        if (launchDarklyClient.isFeatureEnabled("soa-sendgrid")) {
            log.info("******Sendgrid service is enabled****");
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
                log.error("Notification to parties failed");
                throw new IOException(ex.getMessage());
            }
        }
        return EmailNotificationDetails.builder()
            .emailAddress(toEmailAddress)
            .servedParty(servedParty)
            .docs(listOfAttachments.stream().map(ElementUtils::element).toList())
            .attachedDocs(String.join(",", listOfAttachments.stream().map(Document::getDocumentFileName).toList()))
            .timeStamp(currentDate).build();
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
        log.info("from email sendgrid {} ", fromEmailSendgrid);
        Mail mail = new Mail(new Email(fromEmailSendgrid), subject, new Email(toEmailAddress), content);
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
                if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
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
            String documentAsString = "";
            documentAsString = Base64.getEncoder().encodeToString(documentGenService
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
