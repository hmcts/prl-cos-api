
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public EmailNotificationDetails sendEmailWithAttachments(String caseId, String authorization, Map<String, String> emailProps,
                                                             String toEmailAddress, List<Document> listOfAttachments)
        throws IOException {

        String subject = emailProps.get("subject");
        Content content = new Content(emailProps.get("content"), "body");
        Mail mail = new Mail(new Email(fromEmail), subject + caseId, new Email(toEmailAddress), content);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.now();
        String currentDate = datetime.format(formatter);
        if (!listOfAttachments.isEmpty()) {
            attachFiles(authorization, mail, emailProps, listOfAttachments);
        }

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
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
        return EmailNotificationDetails.builder()
            .emailAddress(toEmailAddress)
            .printedDocs(String.join(",", listOfAttachments.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
    }


    private void attachFiles(String authorization, Mail mail, Map<String,
        String> emailProps, List<Document> documents) throws IOException {
        String s2sToken = authTokenGenerator.generate();

        for (Document d : documents) {
            Attachments attachments = new Attachments();
            String documentAsString = "";
            if (d.getDocumentUrl().equalsIgnoreCase("classpath")) {
                documentAsString = Base64.getEncoder().encodeToString(getStaticDocumentAsBytes(d.getDocumentFileName()));
            } else {
                documentAsString = Base64.getEncoder().encodeToString(documentGenService
                                                                         .getDocumentBytes(
                                                                             d.getDocumentUrl(),
                                                                             authorization,
                                                                             s2sToken
                                                                         ));
            }
            attachments.setFilename(d.getDocumentFileName());
            attachments.setType(emailProps.get("attachmentType"));
            attachments.setDisposition(emailProps.get("disposition"));
            attachments.setContent(documentAsString);
            mail.addAttachments(attachments);

        }
    }

    private byte[] getStaticDocumentAsBytes(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(fileName);
        byte [] data = new byte[(int)file.length()];
        try {
            fis.read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        data = bos.toByteArray();
        return data;
    }

}
