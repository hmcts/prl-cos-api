package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, PrePopulateFeeAndSolicitorNameController.class})
public class PrePopulateFeeAndSolicitorControllerIntegrationTest extends IntegrationTest {

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    @Test
    public void givenTemplateAndJsonInput_ReturnStatus200() throws Exception {

        assertTrue(true);

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callPrePopulateFeeAndSolicitorName(requestBody);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void givenEmptyRequestBody_ReturnStatus400() throws Exception {
        Response response = callInvalidPrePopulateFeeAndSolicitorName("");
        assertEquals(400, response.getStatusCode());
    }
}
