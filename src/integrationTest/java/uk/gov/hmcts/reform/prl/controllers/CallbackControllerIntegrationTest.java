package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.prl.DocumentGenerateUtil;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.util.CosApiClient;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;

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

    }

    @Test
    public void givenRequestBodyAndInvalidAuthToken_ReturnStatus401() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callDocGenerateAndSave(requestBody);

        assertEquals(401, response.getStatusCode());
    }
}
