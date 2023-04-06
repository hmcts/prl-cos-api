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
public class ListOnNoticeControllerFT {
    @Autowired
    ObjectMapper objectMapper;

    private static final String LIST_ON_NOTICE_VALID_REQUEST_BODY = "requests/gatekeeping/ListOnNoticeRequest.json";

    private final String userToken = "Bearer testToken";

    private final String listOnNoticeMidEventEndpoint = "/listOnNotice/reasonUpdation/mid-event";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testListOnNoticeWhenReasonsSelected_200ResponseAndNoErrorsFromMidEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeMidEventEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
    }
}
