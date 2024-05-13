package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EditAndApproveDraftOrderControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private DraftAnOrderService draftAnOrderService;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "https://prl-cos-pr-2381.preview.platform.hmcts.net"
        );

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


    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails =  request1
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
        //String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        Map<String, Object> drafOrderMap = new HashMap<>();
        drafOrderMap.put("draftOrder1", "SDO");
        drafOrderMap.put("draftOrder2", "C21");
        Mockito
            .when(draftAnOrderService
                      .getDraftOrderDynamicList(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(drafOrderMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-draft-order-dropdown")
            .then()
            .assertThat().statusCode(200)
            .body("data.draftOrder1", equalTo("SDO"),
                  "data.draftOrder2", equalTo("C21"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_whenJudge_admin_populate_draft_order_then200Response() throws Exception {
        //String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("orderName", "C21");
        caseDataMap.put("orderUploadedAsDraftFlag", "Yes");
        Mockito.when(draftAnOrderService.populateDraftOrderDocument(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/judge-or-admin-populate-draft-order")
            .then()
            .assertThat().statusCode(200)
            .body("data.orderName", equalTo("C21"),
                  "data.orderUploadedAsDraftFlag", equalTo("Yes"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
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
            .andExpect(jsonPath("data.applicantCaseName").value("John Smith"))
            .andExpect(jsonPath("data.caseTypeOfApplication").value("FL401"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_custom_fields_then200Response() throws Exception {
        //String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("appointedGuardianName", "John");
        caseDataMap.put("parentName", "Smith");
        Mockito.when(draftAnOrderService.populateDraftOrderCustomFields(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-custom-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.appointedGuardianName").value("John"))
            .andExpect(jsonPath("data.parentName").value("Smith"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenJudge_or_admin_populate_draft_order_common_fields_then200Response() throws Exception {
        //String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("orderType", "C21");
        caseDataMap.put("isTheOrderByConsent", "Yes");
        Mockito.when(draftAnOrderService
                         .populateCommonDraftOrderFields(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(caseDataMap);
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        mockMvc.perform(post("/judge-or-admin-populate-draft-order-common-fields")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.orderType").value("C21"))
            .andExpect(jsonPath("data.isTheOrderByConsent").value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBodyWhenPostRequestTohandleEditAndApproveSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY1);

        mockMvc.perform(post("/edit-and-approve/submitted")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content()
                           .string(Matchers
                                       .containsString(EditAndApproveDraftOrderController.CONFIRMATION_HEADER)))
            .andReturn();
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo(null))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
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
                  "data.whoApprovedTheOrder", equalTo("SYSTEM_UPDATE"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

}
