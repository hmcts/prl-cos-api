package uk.gov.hmcts.reform.prl.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class FL401SubmitApplicationControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String baseUrl;

    private final String fl401ValidationUrl = "/fl401-submit-application-validation";

    private final String validBody = "requests/FL401-case-data.json";

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Test
    public void whenFL401ValidationRequestShouldReturn200() throws Exception {

        HttpPost httpPost = new HttpPost(baseUrl + "/fl401-submit-application-validation");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse =  HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenFL401SubmitApplicationRequestShouldReturn200() throws Exception {

        String token = idamTokenGenerator.generateIdamTokenForSolicitor();

        HttpPost httpPost = new HttpPost(baseUrl + "/fl401-generate-document-submit-application");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", token);
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse =  HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenFL401SubmitApplicationNotificationRequest() throws Exception {

        HttpPost httpPost = new HttpPost(baseUrl + "/fl401-submit-application-notification");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "Bearer Testtoken");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse =  HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenFl401InvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(baseUrl + "/fl401-generate-document-application");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_NOT_FOUND);
    }

}
