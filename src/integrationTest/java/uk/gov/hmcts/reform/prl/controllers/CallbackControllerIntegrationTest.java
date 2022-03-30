package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, CallbackControllerIntegrationTest.class})
public class CallbackControllerIntegrationTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @Value("${prl.document.generate.uri}")
    protected String documentPrlGenerateUri;

    @Value("${case.orchestration.documentgenerate.uri}")
    protected String documentGenerateUri;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    @Test
    public void testDocumentGenerate_return200() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        HttpGet httpGet = new HttpGet(documentPrlGenerateUri);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
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
}
