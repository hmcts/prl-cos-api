
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
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.json.JsonObject;

import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;

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
            sg.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /*public void sendEmailWithAttachments(Map<String,String> email, CaseData caseData) throws IOException {

        String subject = email.get("subject");
        Content content = new Content("text/plain", " ");
        Attachments attachments = new Attachments();
        if (!getUploadedDocumentsServiceOfApplication(caseData).isEmpty()) {

            attachments.setContent(getUploadedDocumentsServiceOfApplication(caseData));
        }else {
            c100JsonMapper.map(caseData);
            String data = Base64.getEncoder().encodeToString(caseData.toString().getBytes());
            attachments.setContent(data);
        }

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
            sg.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
    }*/

    private List<GeneratedDocumentInfo> getUploadedDocumentsServiceOfApplication(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        Optional<Document> pd36qLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getPd36qLetter());
        Optional<Document> specialArrangementLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                              .getSpecialArrangementsLetter());
        pd36qLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        specialArrangementLetter.ifPresent(document -> docs.add(toGeneratedDocumentInfo(document)));
        return docs;
    }
}
