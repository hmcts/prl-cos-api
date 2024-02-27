package uk.gov.hmcts.reform.prl.controllers;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, SendLetterIntegrationTest.class})
public class SendLetterIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String sendLetter = "/letters";

    private static final String VALID_REQUEST_BODY = "requests/sendLetterBody.json";

    @Test
    public void testSendLetter() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + sendLetter);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("ServiceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }
}
