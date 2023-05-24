
package uk.gov.hmcts.reform.prl.services;

import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
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
import javax.json.JsonObject;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {

    public static final String PRL_RPA_NOTIFICATION = "Private Reform Law CCD Notification ";
    @Value("${send-grid.api-key}")
    private String apiKey;

    @Value("${send-grid.rpa.email.to}")
    private String toEmail;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    private final DocumentGenService documentGenService;

    private final AuthTokenGenerator authTokenGenerator;

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
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            log.info("Initiating email through sendgrid");
            sg.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    public EmailNotificationDetails sendEmailWithAttachments(String authorization, Map<String, String> emailProps,
                                                             String emailAddress, List<Document> listOfAttachments)
        throws IOException {

        String subject = emailProps.get("subject");
        Content content = new Content(emailProps.get("content"), "body");
        Mail mail = new Mail(new Email(fromEmail), subject, new Email(emailAddress), content);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
            log.info("Notification to RPA sent successfully");

        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
        return EmailNotificationDetails.builder()
            .emailAddress(emailAddress)
            .printedDocs(listOfAttachments)
            .timeStamp(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now(ZoneId.of(
                "Europe/London")))).build();
    }


    private void attachFiles(String authorization, Mail mail, Map<String, String> emailProps, List<Document> documents) throws IOException {
        String s2sToken = authTokenGenerator.generate();
        for (Document d : documents) {
            Attachments attachments = new Attachments();
            attachments.setFilename(d.getDocumentFileName());
            attachments.setType(emailProps.get("attachmentType"));
            attachments.setDisposition(emailProps.get("disposition"));
            attachments.setContent(Base64.getEncoder().encodeToString(documentGenService
                                                                          .getDocumentBytes(
                                                                              d.getDocumentUrl(),
                                                                              authorization,
                                                                              s2sToken
                                                                          )));
            mail.addAttachments(attachments);

        }
    }

}
