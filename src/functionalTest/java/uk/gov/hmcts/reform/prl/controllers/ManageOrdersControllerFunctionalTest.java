package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageOrdersControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private static final String VALID_MANAGE_ORDER_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    private static final String VALID_INPUT_JSON_FOR_FINALISE_ORDER = "CallBckReqForFinaliseServeOrder.json";

    private static final String VALID_INPUT_JSON_FOR_DRAFT = "CallBackRequestForDraft.json";

    private static final String VALID_INPUT_JSON_FOR_CREATE_OR_UPLOAD_ORDER = "CallBckReqForCreateOrUpdOrder.json";

    private static final String VALID_CAFCASS_REQUEST_JSON
        = "requests/cafcass-cymru-send-email-request.json";


    private static final String VALID_SERVER_ORDER_REQUEST_JSON
        = "requests/serve-order-send-email-to-app-and-resp-request.json";

    private static final String APPLICANT_CASE_NAME_REQUEST = "requests/call-back-controller-applicant-case-name.json";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static CaseDetails caseDetails;

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setup(){
    }

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

    @Test
    public void givenBody_whenAboutToSubmitForServeOrder() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_FINALISE_ORDER);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBody_whenAboutToSubmitForDraft() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_DRAFT);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBody_whenAboutToSubmitForCreateUpldOrder() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_CREATE_OR_UPLOAD_ORDER);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }






    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());
    }

    @Test
    public void givenRequestBody_WhenPostRequestTestSendCafcassCymruOrderEmail() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails).build();
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(callbackRequest)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then()
            .body("data.postalInformationCaOnlyC47a", equalTo(null))
            .body("data.postalInformationCA", equalTo(null))
            .body("data.otherParties", equalTo(null))
            .body("data.recipientsOptions", equalTo(null))
            .body("data.cafcassCymruEmail", equalTo(null))
            .body("data.serveOrderDynamicList", equalTo(null))
            .body("data.serveOtherPartiesCA", equalTo(null))
            .body("data.cafcassCymruServedOptions", equalTo(null))
            .body("data.emailInformationCaOnlyC47a", equalTo(null))
            .body("data.orderCollection[0].value.serveOrderDetails.cafcassCymruServed",
                  equalTo("Yes"))
            .body("data.orderCollection[0].value.serveOrderDetails.cafcassCymruEmail",
                  equalTo(caseDetails.getData().get("cafcassCymruEmail")))
            .body("data.orderCollection[1].value.serveOrderDetails.cafcassCymruServed",
                  equalTo("Yes"))
            .body("data.orderCollection[1].value.serveOrderDetails.cafcassCymruEmail",
                  equalTo(caseDetails.getData().get("cafcassCymruEmail")));
    }

    @Test
    public void givenRequestBody_WhenServeOrderTestSendEmailToApplicantOrRespLip() throws Exception {

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails.toBuilder()
                             .id(Long.parseLong((String)caseDetails.getData().get("id")))
                             .state(State.JUDICIAL_REVIEW.getLabel()).build()).build();
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(callbackRequest)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then()
            .body("data.id", equalTo(caseDetails.getData().get("id")))
            .assertThat().statusCode(200);
    }

}
