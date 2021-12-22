package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = {FeeAndPayServiceRequestControllerIntegrationTest.class, Application.class})
public class FeeAndPayServiceRequestControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String feeAndPayServiceRequestControllerEndPoint = "/create-payment-service-request";

    private final String path = "CallBackRequest.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + feeAndPayServiceRequestControllerEndPoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenValidRequestFormat_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + feeAndPayServiceRequestControllerEndPoint);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(
            CaseDetails.builder().caseId("1639090820727541")
                .caseData(CaseData.builder()
                              .id(1639090820727541L)
                              .build())
                .build()
        ).build();

        String requestBody = ResourceLoader.loadJson(path);
        httpPost.addHeader("Authorization", "Bearer Test");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity((HttpEntity) callbackRequest);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

}
