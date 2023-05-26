package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AllocateJudgeControllerFT {

    @Autowired
    ObjectMapper objectMapper;

    private static final String LEGAL_ADVISER_PREPOPULATE_VALID_REQUEST_BODY = "controller/valid-request-body.json";

    private static final String ALLOCATE_JUDGE_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest1.json";

    private static final String ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY = "requests/gatekeeping/LegalAdvisorApiRequest.json";

    private static final String ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest2.json";

    private final String prePopulateLegalAdvisersEndpoint = "/allocateJudge/pre-populate-legalAdvisor-details";

    private final String allocateJudgeEndpoint = "/allocateJudge/allocatedJudgeDetails";

    private final String userToken = "Bearer testToken";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testAllocateJudgeWhenTierOfJudiciaryOptionSelected_200ResponseAndNoErrors() throws Exception {

        String requestBody = ResourceLoader.loadJson(ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .header("ServiceAuthorization", "s2sToken")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(allocateJudgeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsValue("circuitJudge"));
    }

    @Test
    public void testAllocateJudgeWhenLegalAdvisorOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .header("ServiceAuthorization", "s2sToken")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateLegalAdvisersEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("legalAdviserList"));

    }

    @Test
    public void testAllocateJudgeWhenJudgeDetailsOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_JUDGE_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .header("ServiceAuthorization", "s2sToken")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(allocateJudgeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("judgeNameAndEmail"));

    }
}
