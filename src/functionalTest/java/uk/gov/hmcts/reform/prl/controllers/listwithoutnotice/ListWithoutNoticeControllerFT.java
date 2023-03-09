package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

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
public class ListWithoutNoticeControllerFT {

    @Autowired
    ObjectMapper objectMapper;

    private static final String LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY = "requests/listwithoutnotice/ListWithoutNotice1.json";

    private static final String LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY2 = "requests/listwithoutnotice/PrePopulateHearingWithExstingRequest.json";
    private static final String LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY3 = "requests/listwithoutnotice/ListWithoutNotice2.json";
    private final String prePopulateHearingPageEndpoint = "/pre-populate-hearingPage-Data";

    private final String listWithoutNoticeEndpoint = "/listWithoutNotice";

    private final String userToken = "Bearer testToken";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testListWithoutNotice_200ResponseAndNoErrors() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listWithoutNoticeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsValue("listWithoutNoticeHearingDetails"));
    }

    @Test
    public void testPrePopulateHearingPageEndpoint_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY2);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("applicantName"));
        Assert.assertTrue(res.getData().containsKey("applicantSolicitor"));
        Assert.assertTrue(res.getData().containsKey("respondentName"));
        Assert.assertTrue(res.getData().containsKey("courtList"));
    }

    @Test
    public void testPrePopulateHearingPageWithExstingData_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY2);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("judgeNameAndEmail"));
        Assert.assertTrue(res.getData().containsKey("hearingJudgeLastName"));
        Assert.assertTrue(res.getData().containsKey("hearingJudgePersonalCode"));
    }
}
