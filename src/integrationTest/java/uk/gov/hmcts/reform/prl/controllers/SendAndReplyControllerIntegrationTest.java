package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SendAndReplyControllerIntegrationTest.class, Application.class})
public class SendAndReplyControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String taskListControllerEndPoint = "/update-task-list/submitted";

    private final String validBody = "requests/send-and-reply-case-data.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/about-to-start");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenInvalidRequestFormatmidEvent_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/mid-event");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenInvalidRequestFormatAboutToSubmit_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/about-to-submit");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenInvalidRequestFormatSubmitted_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/submitted");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenSendAndReplyMidEventValidRequest_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + "/send-and-reply-to-messages/mid-event");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenSendAndReplyAboutToSubmitValidRequest_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + "/send-and-reply-to-messages/about-to-submit");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenSendAndReplySubmittedValidRequest_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + "/send-and-reply-to-messages/submitted");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void whenSendAndReplyAboutToStartValidRequest_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + "/send-and-reply-to-messages/about-start");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }
}
