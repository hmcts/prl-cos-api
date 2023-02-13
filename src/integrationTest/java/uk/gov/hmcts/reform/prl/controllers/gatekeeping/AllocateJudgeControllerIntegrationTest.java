package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AllocateJudgeControllerIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final String prePopulateLegalAdvisersEndpoint = "/allocateJudge/pre-populate-legalAdvisor-details";
    private static final String ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY = "LegalAdvisorApiRequest.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testAllocateJudgeWhenLegalAdvisorOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY);

        Response response = request
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateLegalAdvisersEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("legalAdviserList"));

    }


  /*  @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;


    private final String legalAdvisorEndpoint = "/allocateJudge/pre-populate-legalAdvisor-details";

    private final String allocateJudgeEndpoint = "/allocateJudge/allocatedJudgeDetails";


    private static final String VALID_REQUEST_BODY = "requests/C100-case-data.json";

    private static final String VALID_REQUEST_BODY_JUDGE = "requests/AllocateJudgeDetailsRequest1.json";

    private static final String VALID_REQUEST_BODY_LEGALADVISOR = "requests/LegalAdvisorApiRequest.json";

    private static final String VALID_REQUEST_BODY_TIER_OF_JUDICIARY = "requests/AllocateJudgeDetailsRequest2.json";

    @Test
    public void testPrePoulateLegalAdvisorDetails_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + legalAdvisorEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
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
    public void testAllocateJudgeWhenJudgeDetailsProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_JUDGE);
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
    public void testAllocateJudgeWhenLegalAdvisorDetailsProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_LEGALADVISOR);
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
    public void testAllocateJudgeWhenTierOfJudiciaryProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_LEGALADVISOR);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }*/
}
