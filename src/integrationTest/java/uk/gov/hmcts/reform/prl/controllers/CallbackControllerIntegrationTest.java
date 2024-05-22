package uk.gov.hmcts.reform.prl.controllers;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, CallbackControllerIntegrationTest.class})
public class CallbackControllerIntegrationTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    @Value("${prl.document.generate.uri}")
    protected String documentPrlGenerateUri;

    @Value("${case.orchestration.documentgenerate.uri}")
    protected String documentGenerateUri;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    private static final String PRE_POPULATE_COURT_DETAILS_END_POINT = "/pre-populate-court-details";

    private static final String ALLOCATE_JUDGE_ENDPOINT = "/allocateJudgeTest";

    @Test
    public void testDocumentGenerate_return200() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        HttpGet httpGet = new HttpGet(documentPrlGenerateUri);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader(AUTHORIZATION, "Bearer testauth");
        httpGet.addHeader("serviceAuthorization", "s2sToken");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());

    }

    @Test
    public void givenRequestBodyAndInvalidUrlReturnStatus404() throws Exception {

        HttpPost httpPost = new HttpPost(documentGenerateUri + "test");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testPrePopulateCourtDetails200() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        HttpPost httpPost = new HttpPost(serviceUrl + PRE_POPULATE_COURT_DETAILS_END_POINT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testPrePopulateCourtDetails400() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + PRE_POPULATE_COURT_DETAILS_END_POINT);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader(AUTHORIZATION, "Bearer testauth");
        httpGet.addHeader("serviceAuthorization", "s2sToken");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAllocateJudge200() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        HttpPost httpPost = new HttpPost(serviceUrl + ALLOCATE_JUDGE_ENDPOINT);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAllocateJudge400() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + ALLOCATE_JUDGE_ENDPOINT);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader(AUTHORIZATION, "Bearer testauth");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }
}
