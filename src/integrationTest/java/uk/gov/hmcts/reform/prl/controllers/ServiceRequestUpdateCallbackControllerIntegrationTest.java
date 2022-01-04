package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
//import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {ServiceRequestUpdateCallbackControllerIntegrationTest.class, Application.class})
public class ServiceRequestUpdateCallbackControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")

    protected String serviceUrl;

    private final String serviceRequestContextPath = "/service-request-update";

    private final String path = "CallBackRequest.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + serviceRequestContextPath);

        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }
    /*
    @Ignore
    @Test
    public void whenValidRequestFormat_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + serviceRequestContextPath);
        String requestBody = ResourceLoader.loadJson(path);
        httpPost.addHeader("Authorization", getAuthorizationToken());
        httpPost.addHeader("Authorization", "ServiceAuthorization");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }*/
}
