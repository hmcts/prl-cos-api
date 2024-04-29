package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageOrdersControllerFunctionalTest {

    public static final String MANAGE_ORDERS_VALIDATE_RESPONDENT_AND_OTHER_PERSON_ENDPOINT
        = "/manage-orders/recipients-validations";
    private final String userToken = "Bearer testToken";

    private static final String VALID_MANAGE_ORDER_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    protected RoleAssignmentService roleAssignmentService;

    @MockBean
    private ManageOrderService manageOrderService;

    @MockBean
    private RoleAssignmentApi roleAssignmentApi;

    @MockBean
    private HearingService hearingService;

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    private static final String VALID_INPUT_JSON_FOR_FINALISE_ORDER = "CallBckReqForFinaliseServeOrder.json";

    private static final String VALID_INPUT_JSON_FOR_DRAFT = "CallBackRequestForDraft.json";

    private static final String VALID_INPUT_JSON_FOR_CREATE_OR_UPLOAD_ORDER = "CallBckReqForCreateOrUpdOrder.json";

    private static final String VALID_REQUEST_RESPONDENT_LIP_WITH_NO_ADDRESS
        = "requests/manage-orders/serve-order-request-respondent-lip-noaddress-present.json";

    private static final String VALID_REQUEST_RESPONDENT_LIP_WITH_ADDRESS
        = "requests/manage-orders/serve-order-request-respondent-lip-address-present.json";

    private static final String VALID_REQUEST_OTHER_PARTY_WITH_ADDRESS
        = "requests/manage-orders/serve-order-request-otherParty-address-present.json";

    private static final String VALID_REQUEST_OTHER_PARTY_WITHOUT_ADDRESS
        = "requests/manage-orders/serve-order-request-otherParty-noaddress-present.json";

    public static final String VALID_CAFCASS_REQUEST_JSON
        = "requests/cafcass-cymru-send-email-request.json";

    private MockMvc mockMvc;
    private static final String VALID_SERVER_ORDER_REQUEST_JSON
        = "requests/serve-order-send-email-to-app-and-resp-request.json";

    private static final String APPLICANT_CASE_NAME_REQUEST = "requests/call-back-controller-applicant-case-name.json";

    private static final String VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_ADMIN =
        "CallBckReqForFinaliseServeOrder_courtadmin.json";

    private static final String VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_BAILIFF =
        "CallBckReqForFinaliseServeOrder_courtbailif.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static CaseDetails caseDetails;

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request2 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static final String COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL
        = "requests/court-admin-manage-order-noapproval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_SDO_ORDER_NO_NEED_JUDGE_APPROVAL
        = "requests/court-admin-manage-sdo-order-noapproval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED
        = "requests/court-admin-manage-order-judge-approval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_SDO_ORDER_JUDGE_APPROVAL_REQUIRED
        = "requests/court-admin-manage-sdo-order-judge-approval-required-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED_MANY_HEARING
        = "requests/court-admin-manage-order-judge-approval-required-many-hearing-request.json";

    private static final String COURT_ADMIN_DRAFT_ORDER_MANAGER_APPROVAL_REQUIRED
        = "requests/court-admin-manage-order-manager-approval-required-request.json";

    private static final String JUDGE_DRAFT_ORDER_BODY = "requests/judge-draft-order-request.json";

    private static final String JUDGE_DRAFT_SDO_ORDER_BODY = "requests/judge-draft-order-request.json";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void createCcdTestCase() throws Exception {

        /*String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
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
        Assert.assertNotNull(caseDetails.getId());*/

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        caseDetails = mapper.readValue(json, CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());


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
    public void givenRequestBody_whenPostRequestToPopulateSendManageOrderEmail() throws Exception {

        String requestBodyForCreateCase = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        CaseDetails caseDetails =  request2
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyForCreateCase)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        String requestBodyRevised = requestBody
            .replace("1701870369166430", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then().assertThat().statusCode(200);
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
        when(roleAssignmentApi.updateRoleAssignment(any(), any(), any(), any(RoleAssignmentRequest.class)))
            .thenReturn(RoleAssignmentResponse.builder().build());
        when(hearingService.getHearings(any(), any())).thenReturn(Hearings.hearingsWith().build());
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
    public void givenRequestBody_WhenServeOrderTestSendEmailToApplicantOrRespLip() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails1 = mapper.readValue(json, CaseDetails.class);

        String requestBodyRevised = requestBody
            .replace("1703068453862935", caseDetails1.getId().toString());

        mockMvc.perform(post("/case-order-email-notification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    /**
     * Court Admin manageOrders journey - creates the order with one hearing with no approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_noapproval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_NO_NEED_JUDGE_APPROVAL);

        request
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

    }

    /**
     * Court Admin manageOrders journey - creates the order with one hearings with approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_judge_approval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED);

        String requestBodyRevised = requestBody
            .replace("1702636092071141", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
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
    }

    @Test
    public void givenBodyWhenAddressPresentForOtherPartyShouldNotGetErrorMessage() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_OTHER_PARTY_WITH_ADDRESS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(MANAGE_ORDERS_VALIDATE_RESPONDENT_AND_OTHER_PERSON_ENDPOINT)
            .then()
            .body("data.applicantCaseName",Matchers.equalTo("John Smith"))
            .body("data.caseTypeOfApplication",Matchers.equalTo("C100"));
    }

    /**
     * Court Admin manageOrders journey - creates the order with many hearings with approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_judge_approval_requiredMultiple() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_JUDGE_APPROVAL_REQUIRED_MANY_HEARING);

        String requestBodyRevised = requestBody
            .replace("1702636092071141", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
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

    }

    /**
     * Court Admin manageOrders journey - creates the order with one hearing with manager approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_manager_approval_required() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_ORDER_MANAGER_APPROVAL_REQUIRED);

        request
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
    }

    /**
     * Judge  manageOrders journey - creates the order with one hearing .
     */
    @Test
    public void givenRequestBody_judge_creates_order() throws Exception {
        String requestBody = ResourceLoader.loadJson(JUDGE_DRAFT_ORDER_BODY);

        request
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
    }

    @Test
    public void givenBodyWhenAddressNotPresentForRespondentLipShouldGetErrorMessage() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_RESPONDENT_LIP_WITH_NO_ADDRESS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(MANAGE_ORDERS_VALIDATE_RESPONDENT_AND_OTHER_PERSON_ENDPOINT)
            .then()
            .body("errors", Matchers.contains(ManageOrderService.VALIDATION_ADDRESS_ERROR_RESPONDENT));
    }

    @Test
    public void givenBodyWhenAddressPresentForRespondentLipShouldNotGetErrorMessage() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_RESPONDENT_LIP_WITH_ADDRESS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(MANAGE_ORDERS_VALIDATE_RESPONDENT_AND_OTHER_PERSON_ENDPOINT)
            .then()
            .body("data.applicantCaseName",Matchers.equalTo("John Smith"))
            .body("data.caseTypeOfApplication",Matchers.equalTo("C100"));
    }

    @Test
    public void givenBodyWhenAddressNotPresentOtherPartyShouldGetErrorMessage() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_OTHER_PARTY_WITHOUT_ADDRESS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(MANAGE_ORDERS_VALIDATE_RESPONDENT_AND_OTHER_PERSON_ENDPOINT)
            .then()
            .body("errors", Matchers.contains(ManageOrderService.VALIDATION_ADDRESS_ERROR_OTHER_PARTY));
    }

    @Test
    public void givenRequestBody_WhenPostRequestTestSendCafcassCymruOrderEmail() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBodyRevised = requestBody
            .replace("1703068453862935", caseDetails.getId().toString());

        mockMvc.perform(post("/case-order-email-notification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.postalInformationCaOnlyC47a").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.postalInformationCA").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.otherParties").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.recipientsOptions").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.cafcassCymruEmail").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.serveOrderDynamicList").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.serveOtherPartiesCA").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.cafcassCymruServedOptions").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.emailInformationCaOnlyC47a").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.orderCollection[0].value.serveOrderDetails.cafcassCymruServed")
                           .value("Yes"))
            .andExpect(jsonPath("data.orderCollection[0].value.serveOrderDetails.cafcassCymruEmail")
                           .value(caseDetails.getData().get("cafcassCymruEmail")))
            .andExpect(jsonPath("data.orderCollection[1].value.serveOrderDetails.cafcassCymruServed")
                           .value("Yes"))
            .andExpect(jsonPath("data.orderCollection[1].value.serveOrderDetails.cafcassCymruEmail")
                           .value(caseDetails.getData().get("cafcassCymruEmail")))
            .andReturn();

    }

    @Test
    public void givenBody_ServeOrderForPersonalServiceWithCourtBailiffOptionSelected() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_BAILIFF);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.orderCollection[0].value.serveOrderDetails.courtPersonalService",
                  equalTo(SoaSolicitorServingRespondentsEnum.courtBailiff.name()));

    }

    @Test
    public void givenBody_ServeOrderForPersonalServiceWithCourtAdminOptionSelected() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_ADMIN);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.orderCollection[0].value.serveOrderDetails.courtPersonalService",
                  equalTo(SoaSolicitorServingRespondentsEnum.courtAdmin.name()))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_ForPersonalServiceWhenCourtAdminSelected() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_ADMIN);

        CaseDetails caseDetails =  request2
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

        String requestBodyRevised = requestBody
            .replace("1706997775517206", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then()
            .body("data.recipientsOptions", equalTo(null))
            .body("data.cafcassCymruEmail", equalTo(null))
            .body("data.serveOrderDynamicList", equalTo(null))
            .body("data.serveOtherPartiesCA", equalTo(null))
            .body("data.applicants[0].id", equalTo("97e25c77-f915-4b4e-8436-89a0d1678813"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_ForPersonalServiceWhenBailiffSelected() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_FINALISE_ORDER_COURT_BAILIFF);

        String requestBodyRevised = requestBody
            .replace("1706997775517206", caseDetails.getId().toString());


        mockMvc.perform(post("/case-order-email-notification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.recipientsOptions").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.cafcassCymruEmail").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.serveOrderDynamicList").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.serveOtherPartiesCA").value(IsNull.nullValue()))
            .andExpect(jsonPath("data.applicants[0].id").value("97e25c77-f915-4b4e-8436-89a0d1678813"))
            .andReturn();

        /*request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/case-order-email-notification")
            .then()
            .body("data.recipientsOptions", equalTo(null))
            .body("data.cafcassCymruEmail", equalTo(null))
            .body("data.serveOrderDynamicList", equalTo(null))
            .body("data.serveOtherPartiesCA", equalTo(null))
            .body("data.applicants[0].id", equalTo("97e25c77-f915-4b4e-8436-89a0d1678813"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);*/

    }

    /**
     * Court Admin manageOrders journey - creates the sdo order with one hearing with no approval required.
     */
    @Test
    public void givenRequestBody_courtArdmin_noapproval_required_sdo() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_SDO_ORDER_NO_NEED_JUDGE_APPROVAL);

        String requestBodyRevised = requestBody
            .replace("1706997775517206", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-orders/about-to-submit")
            .then()
            .body("data.isHearingTaskNeeded", equalTo("Yes"),
                  "data.isMultipleHearingSelected", equalTo("No"),
                  "data.hearingOptionSelected", equalTo("dateReservedWithListAssit"),
                  "data.isOrderApproved", equalTo(null),
                  "data.whoApprovedTheOrder", equalTo(null),
                  "data.judgeLaManagerReviewRequired", equalTo("noCheck"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_courtArdmin_judge_approval_required_sdo() throws Exception {
        String requestBody = ResourceLoader.loadJson(COURT_ADMIN_DRAFT_SDO_ORDER_JUDGE_APPROVAL_REQUIRED);

        String requestBodyRevised = requestBody
            .replace("1706997775517206", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
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
    }


    /**
     * Judge  manageOrders journey - creates the sdo order with one hearing .
     */
    @Test
    public void givenRequestBody_judge_creates_sdo_order() throws Exception {
        String requestBody = ResourceLoader.loadJson(JUDGE_DRAFT_SDO_ORDER_BODY);

        request
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
    }
}
