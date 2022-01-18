package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;


@SpringBootTest(classes = {CourtFinderControllerIntegrationTest.class, Application.class})
public class CourtFinderControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String courtFinderEndPoint = "/find-child-arrangements-court";
    private final String requestBody = "courtfinder/court-finder-request.json";
    private final String responseBody = "courtfinder/court-finder-response.json";

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void courtFinderEndPointReturnsCaseDataWithCourtDetails() throws Exception {
        HttpPost httpPost = new HttpPost(serviceUrl + courtFinderEndPoint);
        String jsonString = ResourceLoader.loadJson(requestBody);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(jsonString);
        httpPost.setEntity(body);
        CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        HttpEntity httpEntity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(httpEntity);
        String expectedResponse = ResourceLoader.loadJson(responseBody);

        assertEquals(objectMapper.readTree(expectedResponse), objectMapper.readTree(responseString));

    }

    @Test
    public void endpointReturns200WithValidRequest() throws Exception {
        HttpPost httpPost = new HttpPost(serviceUrl + courtFinderEndPoint);
        String jsonString = ResourceLoader.loadJson(requestBody);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(jsonString);
        httpPost.setEntity(body);
        CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Test
    public void endpointReturns400WithInvalidRequest() throws Exception {
        HttpPost httpPost = new HttpPost(serviceUrl + courtFinderEndPoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        CloseableHttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);

    }



}
