package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageOrdersControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private static final String VALID_MANAGE_ORDER_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private ManageOrderService manageOrderService;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenPostRequestToPopulatePreviewOrder_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-preview-order")
            .then().assertThat().statusCode(200);
    }

    @Test
    @Ignore
    public void givenRequestBody_whenPostRequestToFetchChildList_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/fetch-child-details")
            .then().assertThat().statusCode(500);
    }

    @Test
    @Ignore
    public void givenRequestBody_whenPostRequestToFetchHeader_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-header")
            .then().assertThat().statusCode(200);
    }

    @Ignore
    @Test
    public void givenRequestBody_whenPostRequestToPopulateSendManageOrderEmail() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then().assertThat().statusCode(500);
    }
}
