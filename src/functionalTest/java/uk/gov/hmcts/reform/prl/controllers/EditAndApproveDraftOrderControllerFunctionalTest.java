package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EditAndApproveDraftOrderControllerFunctionalTest {

    private static final String VALID_DRAFT_ORDER_REQUEST_BODY1 = "requests/draft-order-with-options-request.json";
    private static final String VALID_DRAFT_ORDER_REQUEST_BODY = "requests/draft-order-sdo-with-options-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_SOLI_ONE_HEARING_BODY
        = "requests/draft-ordr-judge-edit-approve-soli-1hearing-jugappr-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_SOLI_WITH_MANY_HEARING_BODY
        = "requests/draft-ordr-judge-edit-approve-soli-manyhearing-jugappr-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_SOLI_NO_HEARING_BODY
        = "requests/draft-ordr-judge-edit-approve-soli-nohearing-judgeappr-request.json";
    private static final String DRAFT_ORDER_JUDGE_REJECT_SOLI_ONE_HEARING_BODY
        = "requests/draft-ordr-judge-edit-approve-soli-1hearing-judgereject-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_BODY
        = "requests/judge-edit-approve-court-admin-1hearing-judge-appr-request.json";
    private static final String DRAFT_SDO_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_BODY
        = "requests/judge-edit-approve-court-admin-sdo-1hearing-judge-appr-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_ADMIN_MANY_HEARING_BODY
        = "requests/judge-edit-approve-court-admin-manyhearing-judgeappr-request.json";
    private static final String DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_WITH_2ND_OPTION_BODY
        = "requests/judge-edit-approve-court-admin-1hearing-judge-appr-with2nd-option-request.json";
    private static final String COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL
        = "requests/court-admin-manage-order-noapproval-required-request.json";
    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";
    private static CaseDetails caseDetails;
    private final String userToken = "Bearer testToken";
    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );
    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);
    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;
    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private DraftAnOrderService draftAnOrderService;


    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails = request1
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
    public void givenRequestBody_whenPopulate_draft_order_dropdown_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"event_id\": \"litigationCapacity\"",
                "\"event_id\": \"editAndApproveAnOrder\""
            )

            .replace(
                "\"isJudgeApprovalNeeded\": \"No\"",
                "\"isJudgeApprovalNeeded\": \"Yes\""
            );

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/populate-draft-order-dropdown")
            .then()
            .assertThat().statusCode(200)
            .body("data.draftOrdersDynamicList", notNullValue(),
                  "data.draftOrdersDynamicList.list_items", hasSize(2),
                  "data.caseTypeOfApplication", equalTo("FL401")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_whenJudge_admin_populate_draft_order_then200Response() throws Exception {
        //String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("orderName", "C21");
        caseDataMap.put("orderUploadedAsDraftFlag", "Yes");
        Mockito.when(draftAnOrderService.populateDraftOrderDocument(ArgumentMatchers.any(), ArgumentMatchers.any(),
                                                                    ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"event_id\": \"litigationCapacity\"",
                "\"event_id\": \"editAndApproveAnOrder\""
            )

            .replace(
                "\"isOrderUploadedByJudgeOrAdmin\": \"No\"",
                "\"isOrderUploadedByJudgeOrAdmin\": \"Yes\""
            );

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-populate-draft-order")
            .then()
            .assertThat().statusCode(200)
            .body("data.uploadOrAmendDirectionsFromJudge", equalTo("asdasd"),
                  "data.manageOrderOptionType", equalTo("createAnOrder"),
                  "data.isHearingPageNeeded", equalTo("Yes"),
                  "data.caseTypeOfApplication", equalTo("FL401")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_edit_approve_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.applicantCaseName", equalTo("John Smith"),
                  "data.caseTypeOfApplication", equalTo("FL401")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_custom_fields_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-populate-draft-order-custom-fields")
            .then()
            .assertThat().statusCode(200)
            .body("data.manageOrdersRespondent", equalTo("John Doe"),
                  "data.manageOrdersApplicant", equalTo("test data"),
                  "data.manageOrdersApplicant", equalTo("test data"),
                  "data.isHearingPageNeeded", equalTo("Yes")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_common_fields_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-populate-draft-order-common-fields")
            .then()
            .assertThat().statusCode(200)
            .body("data.orderType", equalTo("noticeOfProceedingsNonParties"),
                  "data.uploadOrAmendDirectionsFromJudge", equalTo("asdasd"),
                  "data.isCafcassCymru", equalTo("Yes"),
                  "data.isFL401RespondentSolicitorPresent", equalTo("Yes"),
                  "data.instructionsFromJudge", equalTo("asdasd"),
                  "data.draftOrdersDynamicList", equalTo("fa632e84-bc22-4d74-bd36-b37cd27095d3")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBodyWhenPostRequestTohandleEditAndApproveSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);

        String requestBodyRevised = requestBody
            .replace("1711105989241323", caseDetails.getId().toString());

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/edit-and-approve/submitted")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - approves the order with one hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_ONE_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateReservedWithListAssit"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - approves the order with many hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_many_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_WITH_MANY_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("Yes"),
                  "data.hearingOptionSelected", equalTo("multipleOptionSelected"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - approves the order with no hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_with_no_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_NO_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo(null),
                  "data.isMultipleHearingSelected", equalTo(null),
                  "data.hearingOptionSelected", equalTo(null),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - rejects the order with one hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_reject_soli_order_with_one_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_REJECT_SOLI_ONE_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"), // revamp
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateReservedWithListAssit"),
                  "data.isOrderApproved", equalTo("No"),
                  "data.whoApprovedTheOrder", equalTo(null)
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    //Court admin

    /**
     * Judge editApprove - approves the order with one hearing which is created by court admin.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateConfirmedByListingTeam"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - approves the order with many hearings which is created by court admin.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_order_with_many_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_MANY_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("Yes"),
                  "data.hearingOptionSelected", equalTo("multipleOptionSelected"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }


    /**
     * Judge editApprove - approves the order with one hearing with
     * 2nd hearing option() which is created by court admin.
     * then response should be isHearingTaskNeeded as 'No'
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_with2ndOption_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_WITH_2ND_OPTION_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateConfirmedInHearingsTab"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Judge editApprove - approves the sdo order with one hearing which is created by court admin.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_Sdo_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_SDO_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_BODY);

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateReservedWithListAssit"),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

}
