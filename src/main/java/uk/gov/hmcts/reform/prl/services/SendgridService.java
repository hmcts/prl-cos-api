
package uk.gov.hmcts.reform.prl.services;

import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.TransferCaseTemplate;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonObject;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.SPECIAL_INSTRUCTIONS_EMAIL_BODY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {

    public static final String PRL_RPA_NOTIFICATION = "Private Reform Law CCD Notification ";
    public static final String MAIL_SEND = "mail/send";
    public static final String CASE_NAME = "caseName";
    @Value("${send-grid.api-key}")
    private String apiKey;

    @Value("${send-grid.rpa.email.to}")
    private String toEmail;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    private final DocumentGenService documentGenService;

    private final AuthTokenGenerator authTokenGenerator;

    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    ResourceLoader resourceLoader;

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
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            log.info("Initiating email through sendgrid");
            sg.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public EmailNotificationDetails sendEmailWithAttachments(String authorization, Map<String, String> emailProps,
                                                             String toEmailAddress, List<Document> listOfAttachments,String servedParty)
        throws IOException {

        String subject = emailProps.get("subject");
        Content content = new Content("text/plain", String.format(
            (emailProps.containsKey("specialNote") && emailProps.get("specialNote")
                .equalsIgnoreCase("Yes")) ? SPECIAL_INSTRUCTIONS_EMAIL_BODY : EMAIL_BODY,
            emailProps.get(CASE_NAME),
            emailProps.get("caseNumber"),
            emailProps.get("solicitorName")
        ));
        Mail mail = new Mail(new Email(fromEmail), subject + emailProps.get(CASE_NAME), new Email(toEmailAddress), content);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }

        if (launchDarklyClient.isFeatureEnabled("soa-sendgrid")) {
            log.info("******Sendgrid service is enabled****");
            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint(MAIL_SEND);
                request.setBody(mail.build());
                Response response = sg.api(request);
                log.info("Sendgrid status code {}", response.getStatusCode());
                if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                    log.info("Notification to party sent successfully");
                }

            } catch (IOException ex) {
                log.error("Notification to parties failed");
                throw new IOException(ex.getMessage());
            }
        }
        return EmailNotificationDetails.builder()
            .emailAddress(toEmailAddress)
            .servedParty(servedParty)
            .docs(listOfAttachments.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", listOfAttachments.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
    }

    public void sendTransferCourtEmailWithAttachments(String authorization, Map<String, String> emailProps,
                                                             String toEmailAddress, List<Document> listOfAttachments)
        throws IOException {
        String subject = emailProps.get("subject");
        Content content = new Content("text/html", String.format(
            TransferCaseTemplate.TRANSFER_CASE_EMAIL_BODY,
            emailProps.get("caseNumber"),
            emailProps.get(CASE_NAME),
            emailProps.get("issueDate"),
            emailProps.get("applicationType"),
            emailProps.get("confidentialityText"),
            emailProps.get("courtName")
        ));
        Mail mail = new Mail(new Email(fromEmail), subject + emailProps.get(CASE_NAME), new Email(toEmailAddress), content);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }
        if (launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")) {
            log.info("******Sendgrid service is enabled****");
            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint(MAIL_SEND);
                request.setBody(mail.build());
                Response response = sg.api(request);
                log.info("Sendgrid status code {}", response.getStatusCode());
                if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
                    log.info("Notification to party sent successfully");
                }

            } catch (IOException ex) {
                log.error("Notification to parties failed");
                throw new IOException(ex.getMessage());
            }
        }
    }

    private void attachFiles(String authorization, Mail mail, Map<String,
        String> emailProps, List<Document> documents) throws IOException {
        String s2sToken = authTokenGenerator.generate();

        for (Document d : documents) {
            Attachments attachments = new Attachments();
            String documentAsString = "";
            documentAsString = Base64.getEncoder().encodeToString(documentGenService
                                                                      .getDocumentBytes(
                                                                          d.getDocumentUrl(),
                                                                          authorization,
                                                                          s2sToken
                                                                      ));
            attachments.setFilename(d.getDocumentFileName());
            attachments.setType(emailProps.get("attachmentType"));
            attachments.setDisposition(emailProps.get("disposition"));
            attachments.setContent(documentAsString);
            /*attachments.setContent(Base64.getEncoder().encodeToString(documentGenService
                                                                      .getDocumentBytes(
                                                                          d.getDocumentUrl(),
                                                                          authorization,
                                                                          s2sToken
                                                                      )));*/
            mail.addAttachments(attachments);

        }
    }

}
