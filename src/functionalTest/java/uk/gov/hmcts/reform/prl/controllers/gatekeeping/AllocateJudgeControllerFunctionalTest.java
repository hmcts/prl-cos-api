package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
@SpringBootTest
@ContextConfiguration
public class AllocateJudgeControllerFunctionalTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String ALLOCATE_JUDGE_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest1.json";

    private static final String ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY = "requests/gatekeeping/LegalAdvisorApiRequest.json";

    private static final String ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest2.json";

    private static final String LEGAL_ADVISER_URL = "/allocateJudge/pre-populate-legalAdvisor-details";

    private static final String ALLOCATE_JUDGE_URL = "/allocateJudge/allocatedJudgeDetails";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testAllocateJudgeWhenTierOfJudiciaryOptionSelected200ResponseAndNoErrors() throws Exception {

        String requestBody = ResourceLoader.loadJson(ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(ALLOCATE_JUDGE_URL);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        assertNotNull(res.getData());
        assertTrue(res.getData().containsValue("circuitJudge"));
    }

    @Test
    public void testAllocateJudgeWhenLegalAdvisorOptionSelected200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(LEGAL_ADVISER_URL);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        assertNotNull(res.getData());
        assertTrue(res.getData().containsKey("legalAdviserList"));

    }

    @Test
    @Disabled
    public void testAllocateJudgeWhenJudgeDetailsOptionSelected200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_JUDGE_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(ALLOCATE_JUDGE_URL);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        assertNotNull(res.getData());
        assertTrue(res.getData().containsKey("judgeNameAndEmail"));

    }
}
