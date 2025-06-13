package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, SendGridIntegrationTest.class})
public class SendGridIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    public static final String MAIL_SEND = "mail/send";

    private static final String VALID_REQUEST_BODY = "requests/sendgridBody.json";

    @Test
    public void testSendEmailWithAttachments() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + MAIL_SEND);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("ServiceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }
}
