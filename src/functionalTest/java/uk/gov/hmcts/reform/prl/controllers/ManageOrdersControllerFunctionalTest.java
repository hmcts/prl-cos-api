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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;


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

    private static final String VALID_INPUT_JSON_FOR_FINALISE_ORDER = "CallBckReqForFinaliseServeOrder.json";

    private static final String VALID_INPUT_JSON_FOR_DRAFT = "CallBackRequestForDraft.json";

    private static final String VALID_INPUT_JSON_FOR_CREATE_OR_UPLOAD_ORDER = "CallBckReqForCreateOrUpdOrder.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static final String COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL
        = "requests/court-admin-manage-order-noapproval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED
        = "requests/court-admin-manage-order-judge-approval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED_MANY_HEARING
        = "requests/court-admin-manage-order-judge-approval-required-many-hearing-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_MANAGER_APPROVAL_REQUIRED
        = "requests/court-admin-manage-order-manager-approval-required-request.json";

    private static final String JUDGE_DRAFT_ORDER_BODY = "requests/judge-draft-order-request.json";



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
    public void givenRequestBody_whenPostRequestToFetchChildList_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/fetch-child-details")
            .then().assertThat().statusCode(200);
    }

    @Test
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

    @Test
    @Ignore
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

    /**
     * Court Admin manageOrders journey - creates the order with one hearing with no approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_noapproval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL);

        AboutToStartOrSubmitCallbackResponse resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateToBeFixed"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo("noCheck"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData());

    }

    /**
     * Court Admin manageOrders journey - creates the order with one hearings with approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_judge_approval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED);

        AboutToStartOrSubmitCallbackResponse resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateReservedWithListAssit"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo("judgeOrLegalAdvisorCheck"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData());

    }


    /**
     * Court Admin manageOrders journey - creates the order with many hearings with approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_judge_approval_requiredMultiple() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED_MANY_HEARING);

        AboutToStartOrSubmitCallbackResponse resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"),// shud be no
                  "data.isMultipleHearingSelected", equalTo("Yes"),
                  "data.hearingOptionSelected", equalTo("multipleOptionSelected"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo("judgeOrLegalAdvisorCheck"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Court Admin manageOrders journey - creates the order with one hearing with manager approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_manager_approval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_MANAGER_APPROVAL_REQUIRED);

        AboutToStartOrSubmitCallbackResponse resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateToBeFixed"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo("managerCheck"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Judge  manageOrders journey - creates the order with one hearing .
     */
    @Test
    public void givenRequestBody_judge_creates_order() throws Exception {
        String requestBody = ResourceLoader.loadJson(JUDGE_DRAFT_ORDER_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateToBeFixed"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }
}
