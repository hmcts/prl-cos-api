package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ManageDocumentIntegrationTest.class, Application.class})
public class ManageDocumentIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;


    private final String validBody = "requests/manage-documents-request.json";

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

}
