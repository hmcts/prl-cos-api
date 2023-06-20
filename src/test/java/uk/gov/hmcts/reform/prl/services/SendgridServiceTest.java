package uk.gov.hmcts.reform.prl.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.io.IOException;
import javax.json.JsonObject;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendgridServiceTest {

    @InjectMocks
    private SendgridService sendgridService;

    @Mock
    private SendGrid sendGrid;

    @Test(expected = IOException.class)
    public void testSendEmailInvokingSendGridApi() throws IOException {
        Response response = new Response();
        response.setStatusCode(200);
        JsonObject jsonObject = new NullAwareJsonObjectBuilder()
            .add("applicantCaseName","hello")
            .build();
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        sendgridService.sendEmail(jsonObject);
        verify(sendGrid,times(1)).api(request);
    }

    /*@Test
    public void testGetStaticDocumentAsString() throws IOException, URISyntaxException {

        String s = sendgridService.getStaticDocumentAsString("Privacy_Notice.pdf");
    }*/
}
