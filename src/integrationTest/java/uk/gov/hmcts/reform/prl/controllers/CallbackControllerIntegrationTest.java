package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.DocumentGenerateUtil;
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

    @Value("${case.orchestration.documentgenerate.uri}")
    protected String documentGenerateUri;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";


    @DisplayName("temporary test to verify gov UK notifications integration")
    @Test
    public void sendEmail() {
        cosApiClient.sendEmail(CallbackRequest.builder()
                                   .caseDetails(CaseDetails.builder().build())
                                   .build());
    }

    @Test
    public void testDocumentGenerate_return200() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callDocGenerateAndSave(requestBody);

        assertEquals(200, response.getStatusCode());

        /*HttpPost httpPost = new HttpPost(documentGenerateUri);
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());*/

    }

    @Test
    public void givenRequestBodyAndInvalidAuthToken_ReturnStatus401() throws Exception {

        HttpPost httpPost = new HttpPost(documentGenerateUri + "test");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_NOT_FOUND);
    }

}
