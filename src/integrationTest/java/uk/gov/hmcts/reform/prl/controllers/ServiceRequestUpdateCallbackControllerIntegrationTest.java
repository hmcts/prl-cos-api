package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
//import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceRequestUpdateCallbackControllerIntegrationTest.class, Application.class})
public class ServiceRequestUpdateCallbackControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")

    protected String serviceUrl;

    private final String serviceRequestContextPath = "/service-req-update";

    private final String path = "requests/C100-case-data.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + serviceRequestContextPath);

        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void whenServiceRequestUpdateRequest() throws Exception {

        HttpPut httpPut = new HttpPut(serviceUrl + serviceRequestContextPath);
        String requestBody = ResourceLoader.loadJson(path);
        httpPut.addHeader("Authorization", "Bearer TestAuth");
        httpPut.addHeader("Authorization", "ServiceAuthorization");
        httpPut.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPut.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPut);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }
}
