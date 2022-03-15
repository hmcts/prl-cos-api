package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
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
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, PrePopulateFeeAndSolicitorNameController.class})
public class PrePopulateFeeAndSolicitorControllerIntegrationTest extends IntegrationTest {

    private static final String VALID_INPUT_JSON = "controller/valid-request-body.json";

    @Value("${case.orchestration.prepopulate.uri}")
    protected String prePopulateUri;

    @Test
    public void givenTemplateAndJsonInput_ReturnStatus200() throws Exception {

        assertTrue(true);

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        HttpPost httpPost = new HttpPost(prePopulateUri);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void givenEmptyRequestBody_ReturnStatus400() throws Exception {
        Response response = callInvalidPrePopulateFeeAndSolicitorName("");
        assertEquals(400, response.getStatusCode());
    }
}
