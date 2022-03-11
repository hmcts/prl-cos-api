
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import javax.json.JsonObject;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {

    @Value("${send-grid.api-key}")
    private String apiKey;

    @Value("${send-grid.rpa.email.to}")
    private String toEmail;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    public void sendEmail(JsonObject caseData) throws IOException {

        String subject = "Private Reform Law CCD Notification " + caseData.get("id");
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
            Response response = sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }
}
