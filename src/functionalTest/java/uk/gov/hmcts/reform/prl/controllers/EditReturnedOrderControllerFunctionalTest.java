package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.services.EditReturnedOrderService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EditReturnedOrderControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EditReturnedOrderService editReturnedOrderService;

    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

    private static CaseDetails caseDetails;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        RestAssured.registerParser("text/html", Parser.JSON);
    }


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static final String VALID_INPUT_JSON_FOR_RETURNED_ORDER = "requests/editreturnedorder/CallBackRequestEditReturnedOrder.json";

    @Test
    public void createCcdTestCase() throws Exception {

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
    public void givenRequestBody_whenAboutToStart_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_RETURNED_ORDER);
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/edit-returned-order/about-to-start");
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertTrue(res.getData().containsKey("rejectedOrdersDynamicList"));
        DynamicList rejectedOrdersDynamicList = objectMapper.convertValue(
            res.getData().get("rejectedOrdersDynamicList"),
            DynamicList.class
        );
        Assert.assertNotNull(rejectedOrdersDynamicList);
    }

    @Test
    public void givenBody_whenMidEventToPopulateInstructions() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_RETURNED_ORDER);
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/edit-returned-order/mid-event/populate-instructions");
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertTrue(res.getData().containsKey("editOrderTextInstructions"));
        String editOrderTextInstructions = objectMapper.convertValue(
            res.getData().get("editOrderTextInstructions"),
            String.class
        );
        Assert.assertNotNull(editOrderTextInstructions);

    }

    @Test
    public void givenBody_whenSubmittedToResubmit() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON_FOR_RETURNED_ORDER);
        String requestBodyRevised = requestBody
            .replace("1706607610239516", caseDetails.getId().toString());
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/edit-returned-order/submitted");
        response.then().assertThat().statusCode(200);
        SubmittedCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            SubmittedCallbackResponse.class
        );
        Assert.assertEquals(res.getConfirmationHeader(), "# Draft order resubmitted");

    }
}
