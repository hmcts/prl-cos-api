package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class DraftOrdersControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private ManageOrderService manageOrderService;

    @MockBean
    private DraftAnOrderService draftAnOrderService;

    @MockBean
    private HearingDataService hearingDataService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;


    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
    private static final String VALID_DRAFT_ORDER_REQUEST_BODY = "requests/draft-order-sdo-with-options-request.json";

    private static final String AUTHORIZATION = "Authorization";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private static final String SELECTED_ORDER = "/selected-order";



    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenRequestBody_whenReset_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/reset-fields").then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals("C100",response.getData().get("caseTypeOfApplication"));


    }

    @Test
    public void givenRequestBody_whenSelected_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(SELECTED_ORDER)
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals("This order is not available to be drafted",response.getErrors().get(0));

    }

    @Test
    public void givenRequestBody_whenPopulate_draft_order_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);


        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE).id(Long.parseLong("1647373355918192"))
            .build();
        when(manageOrderService.populateCustomOrderFields(any(), any())).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-draft-order-fields")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1705065178030549L,response.getData().get("id"));
        Assert.assertEquals("John Smith",response.getData().get("applicantCaseName"));
        Assert.assertEquals("FL401",response.getData().get("caseTypeOfApplication"));
        Assert.assertEquals("createAnOrder",response.getData().get("manageOrdersOptions"));
        Assert.assertEquals("standardDirectionsOrder",response.getData().get("createSelectOrderOptions"));
        Assert.assertEquals("draftAnOrder",response.getData().get("draftOrderOptions"));
        Assert.assertEquals("c21ApplicationRefused",response.getData().get("c21OrderOptions"));
        Assert.assertEquals("fa632e84-bc22-4d74-bd36-b37cd27095d3",response.getData().get("draftOrdersDynamicList"));

    }

    @Test
    public void givenRequestBody_whenPopulate_standard_direction_order_fields() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-standard-direction-order-fields")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1705065178030549L,response.getData().get("id"));
        Assert.assertEquals("John Smith",response.getData().get("applicantCaseName"));
        Assert.assertEquals("FL401",response.getData().get("caseTypeOfApplication"));
        Assert.assertEquals("createAnOrder",response.getData().get("manageOrdersOptions"));
        Assert.assertEquals("standardDirectionsOrder",response.getData().get("createSelectOrderOptions"));
        Assert.assertEquals("draftAnOrder",response.getData().get("draftOrderOptions"));
        Assert.assertEquals("c21ApplicationRefused",response.getData().get("c21OrderOptions"));
        Assert.assertEquals("Yes",response.getData().get("isCafcassCymru"));
        Assert.assertEquals("Yes",response.getData().get("isFL401RespondentPresent"));
        Assert.assertEquals("Yes",response.getData().get("isApplicant1Present"));
        Assert.assertEquals("Yes",response.getData().get("isFL401ApplicantPresent"));
    }

    @Test
    public void givenRequestBody_whenAbout_to_submit() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        Map<String, Object> caseDataMap = new HashMap<>();
        when(draftAnOrderService.prepareDraftOrderCollection(
            anyString(),
           any(CallbackRequest.class)
        )).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1705065178030549L,response.getData().get("id"));
        Assert.assertEquals("John Smith",response.getData().get("applicantCaseName"));
        Assert.assertEquals("FL401",response.getData().get("caseTypeOfApplication"));
        Assert.assertEquals("fa632e84-bc22-4d74-bd36-b37cd27095d3",response.getData().get("draftOrdersDynamicList"));

    }


    @Test
    public void givenRequestBody_whenGenerate_doc() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().c21OrderOptions(
                C21OrderOptionsEnum.c21NoOrderMade).build())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(manageOrderService.getCaseData(
            "test",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        )).thenReturn(caseDataMap);

        AboutToStartOrSubmitCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-doc")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1705065178030549L,response.getData().get("id"));
        Assert.assertEquals("John Smith",response.getData().get("applicantCaseName"));
        Assert.assertEquals("FL401",response.getData().get("caseTypeOfApplication"));
        Assert.assertEquals("createAnOrder",response.getData().get("manageOrdersOptions"));
        Assert.assertEquals("standardDirectionsOrder",response.getData().get("createSelectOrderOptions"));
        Assert.assertEquals("draftAnOrder",response.getData().get("draftOrderOptions"));
        Assert.assertEquals("c21ApplicationRefused",response.getData().get("c21OrderOptions"));
    }


    /**
     * When selected order id for DA case is 'FL402'.
     * then error should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenFl402OrderSelectedForDA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                     "\"createSelectOrderOptions\": \"noticeOfProceedings\"");

        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(SELECTED_ORDER)
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'Fl402'.
     * then error should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenFl402OrderSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedings\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                      "\"caseTypeOfApplication\": \"C100\"");

        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(SELECTED_ORDER)
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'NoticeOfProceeding'.
     * then error msg should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenNoticeOfProceedingSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedingsParties\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                "\"caseTypeOfApplication\": \"C100\"");

        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(SELECTED_ORDER)
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'noticeOfProceedingsNonParties'.
     * then error msg should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenNoticeOfProceedingNonPartiesSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedingsNonParties\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                "\"caseTypeOfApplication\": \"C100\"");

        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(SELECTED_ORDER)
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }



}
