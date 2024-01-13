package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class EditAndApproveDraftOrderControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String VALID_DRAFT_ORDER_REQUEST_BODY1 = "requests/draft-order-with-options-request.json";

    private static final String VALID_DRAFT_ORDER_REQUEST_BODY2 = "requests/draft-order-sdo-with-options-request.json";

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

    private static final String DRAFT_ORDER_JUDGE_APPRV_ADMIN_MANY_HEARING_BODY
        = "requests/judge-edit-approve-court-admin-manyhearing-judgeappr-request.json";

    private static final String DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_WITH_2ND_OPTION_BODY
        = "requests/judge-edit-approve-court-admin-1hearing-judge-appr-with2nd-option-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL
        = "requests/court-admin-manage-order-noapproval-required-request.json";

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenRequestBody_whenPopulate_draft_order_dropdown_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/populate-draft-order-dropdown")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_admin_populate_draft_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_edit_approve_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/judge-or-admin-edit-approve/about-to-submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_custom_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-custom-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_common_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-common-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    @Ignore
    public void givenRequestBodyWhenPostRequestTohandleEditAndApproveSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        mockMvc.perform(post("/edit-and-approve/submitted")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    /**
     * Judge editApprove - approves the order with one hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_ONE_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Judge editApprove - approves the order with many hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_many_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_WITH_MANY_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Judge editApprove - approves the order with no hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_soli_order_with_no_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_SOLI_NO_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-edit-approve/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("No"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo(null),
                  "data.isOrderApproved", equalTo("Yes"),
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Judge editApprove - rejects the order with one hearing which is created by solicitor.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_reject_soli_order_with_one_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_REJECT_SOLI_ONE_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    //Court admin

    /**
     * Judge editApprove - approves the order with one hearing which is created by court admin.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }

    /**
     * Judge editApprove - approves the order with many hearings which is created by court admin.
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_order_with_many_hearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_MANY_HEARING_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }


    /**
     * Judge editApprove - approves the order with one hearing with
     * 2nd hearing option() which is created by court admin.
     * then response should be isHearingTaskNeeded as 'No'
     */
    @Test
    public void givenRequestBody_whenJudge_edit_approve_court_admin_with2ndOption_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(DRAFT_ORDER_JUDGE_APPRV_ADMIN_ONE_HEARING_WITH_2ND_OPTION_BODY);

        AboutToStartOrSubmitCallbackResponse resp = request1
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        System.out.println("Respppp " + resp.getData().get("isHearingTaskNeeded"));

    }
}
