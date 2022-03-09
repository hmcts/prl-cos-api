package uk.gov.hmcts.reform.prl.services;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;


import javax.json.JsonObject;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendgridServiceTest {

    @InjectMocks
    private SendgridService sendgridService;

    @Mock
    private SendGrid sendGrid;

    @Test
    public void TestSendEmail() throws IOException {
        Response response = new Response();
        response.setStatusCode(200);
        JsonObject jsonObject = new NullAwareJsonObjectBuilder()
            .add("applicantCaseName","hello")
            .build();
        sendGrid = new SendGrid(null);
        when(sendGrid.api(Mockito.any(Request.class))).thenReturn(response);
        sendgridService.sendEmail(jsonObject);
        //assertThrows(sendgridService.sendEmail(jsonObject),IOException);
        assertEquals(verify(sendGrid.api(Mockito.any(Request.class))).getStatusCode(),200);
    }
}
