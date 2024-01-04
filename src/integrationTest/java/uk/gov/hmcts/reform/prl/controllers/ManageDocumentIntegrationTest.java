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

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ManageDocumentIntegrationTest.class, Application.class})
public class ManageDocumentIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;


    private final String validBody = "requests/manage-documents-request.json";

    private final String copyManageDocsMidEndpoint = "/copy-manage-docs-mid";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/manage-documents/about-to-start");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenInvalidRequestFormatCopyManageDocs_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/manage-documents/copy-manage-docs");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenInvalidRequestFormatSubmitted_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + "/manage-documents/submitted");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testPopulatePreviewOrderEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(validBody);
        HttpPost httpPost = new HttpPost(serviceUrl + copyManageDocsMidEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

}
