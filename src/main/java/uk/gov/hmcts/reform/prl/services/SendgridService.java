
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

import javax.json.JsonObject;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendgridService {

    @Value("${send-grid.api-key}")
    private String apiKey;

    public void sendEmail(JsonObject caseData) throws IOException {
        Email from = new Email("sairam@mail-prl-nonprod.aat.platform.hmcts.net");
        String subject = "Notification to RPA to create case in Familyman";
        Email to = new Email("swaroopa.pendyala@HMCTS.NET");
        //Email to = new Email("bharadwajsairam.manchella@HMCTS.NET");
        //Content content = new Content("application/json", JSONObject.valueToString(caseData));
        Content content = new Content("text/plain", "PFA the data to create a case in Family man");
        Mail mail = new Mail(from, subject, to, content);

        Attachments attachments = new Attachments();
        String data = Base64.getEncoder().encodeToString(caseData.toString().getBytes());
        attachments.setContent(data);
        attachments.setFilename("Casedata.json");
        attachments.setType("application/json");
        attachments.setDisposition("attachment");
        mail.addAttachments(attachments);
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
