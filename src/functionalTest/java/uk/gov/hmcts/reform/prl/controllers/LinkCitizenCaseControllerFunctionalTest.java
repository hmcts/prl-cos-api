package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LinkCitizenCaseControllerFunctionalTest {

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

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/create-case-with-access-coderequest.json";

    private static CaseDetails caseDetails1;

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request2 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        RestAssured.registerParser("text/html", Parser.JSON);
    }

    private static final String CITIZEN_REQUEST_BODY
        = "requests/link-citizen-case-access-code.json";

    private static final String CITIZEN_REQUEST_BODY1
        = "requests/link-citizen-case-access-code1.json";


    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

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
        caseDetails1 = mapper.readValue(json, CaseDetails.class);

        Assert.assertNotNull(caseDetails1);
        Assert.assertNotNull(caseDetails1.getId());


    }

    @Test
    public void givenRequestBody_linkCitizenToCaseWithHearing_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody
            .replace("1711626009844772", caseDetails1.getId().toString());

        CaseDataWithHearingResponse response = request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/link-case-to-account-with-hearing")
            .then()
            .extract()
            .as(CaseDataWithHearingResponse.class);
        Assert.assertNotNull(response);
    }

    @Test
    public void givenRequestBody_linkCaseToAccount_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY);
        String requestBodyRevised = requestBody
            .replace("1711626009844770", caseDetails1.getId().toString());

        mockMvc.perform(post("/citizen/link-case-to-account")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("caseInvites[0].value.accessCode").value("FVJKGHF"))
            .andExpect(jsonPath("caseInvites[0].value.hasLinked").value(YesOrNo.Yes.toString()))
            .andReturn();
    }

    @Test
    public void givenRequestBody_validateAccessCode_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody
            .replace("1711626009844772", caseDetails1.getId().toString());

        MvcResult res = mockMvc.perform(post("/citizen/validate-access-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBodyRevised)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();
        Assert.assertEquals("Linked",json);
    }

    @Test
    public void givenRequestBody_validateAccessCode_then200Response11() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        CaseDetails caseDetails3 =  request1
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

        Assert.assertNotNull(caseDetails3);
        Assert.assertNotNull(caseDetails3.getId());

        String requestBody1 = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody1
            .replace("1711626009844772", caseDetails3.getId().toString());
        String response = request2
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/validate-access-code")
            .then()
            .extract()
            .asString();
        Assert.assertNotNull(response);
        Assert.assertEquals("Valid",response);
    }
}
