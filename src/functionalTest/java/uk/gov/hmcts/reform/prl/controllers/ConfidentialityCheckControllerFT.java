package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.APPLICATION_SERVED_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ConfidentialityCheckControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    public void givenRequestWithCaseData_ResponseContains() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/confidentiality-check/about-to-start")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.unServedApplicantPack.packDocument").doesNotExist())
            .andExpect(jsonPath("data.unServedApplicantPack.partyIds").doesNotExist())
            .andReturn();
    }

    @Test
    public void givenRequestWithCaseData_ResponseContainsYesOrNo() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        doNothing().when(coreCaseDataService).triggerEvent(anyString(), anyString(), anyLong(), anyString(), anyMap());
        MvcResult res = mockMvc.perform(post("/confidentiality-check/submitted")
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
        assertTrue(json.contains(APPLICATION_SERVED_HEADER));
    }

    @Test
    public void givenRequestBody_whenConfidentialCheckWhenAppplicationServedAndUnServedRespondentPackAvailable() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"confidentialityCheck\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo("Yes"),
                  "data.responsibleForService", equalTo("Court admin"),
                  "data.isC8CheckNeeded", equalTo(null),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenConfidentialCheckWhenApplicationServedAndUnServedRespondentPackNotAvailable() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"confidentialityCheck\"")
            .replace("\"personalServiceBy\": \"Court admin\"",
                     "\"removepersonalServiceBy\":  \"\"")
            .replace("\"applicationServedYesNo\": \"Yes\"",
                     "\"applicationServedYesNo\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo("No"),
                  "data.responsibleForService", equalTo(null),
                  "data.isC8CheckNeeded", equalTo(null),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventC100WhenBothIsConfidentialAndSoaServeToRespondentOptionsYes() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo("Applicant's legal representative"),
                  "data.isC8CheckNeeded", equalTo("Yes"),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventC100WhenBothIsConfidentialAndSoaServeToRespondentOptionsNo() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"soaServeToRespondentOptions\": \"Yes\"",
                 "\"soaServeToRespondentOptions\": \"No\"")
            .replace("\"isConfidential\": \"Yes\"",
                     "\"isConfidential\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo(null),
                  "data.isC8CheckNeeded", equalTo("No"),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventFl401WhenBothIsConfidentialAndSoaServingRespondentsOptionsDaYes() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"caseTypeOfApplication\": \"C100\"",
                 "\"caseTypeOfApplication\": \"FL401\"")
            .replace("\"caseCreatedBy\": \"SOLICITOR\"",
                     "\"caseCreatedBy\": \"CITIZEN\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo("Court bailiff"),
                  "data.isC8CheckNeeded", equalTo("Yes"),
                  "data.isOccupationOrderSelected", equalTo("Yes"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventFL401WhenIsConfidentialNo() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"caseTypeOfApplication\": \"C100\"",
                     "\"caseTypeOfApplication\": \"FL401\"")
            .replace("\"caseCreatedBy\": \"SOLICITOR\"",
                     "\"caseCreatedBy\": \"CITIZEN\"")
            .replace("\"isConfidential\": \"Yes\"",
                     "\"isConfidential\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo("Court bailiff"),
                  "data.isC8CheckNeeded", equalTo("No"),
                  "data.isOccupationOrderSelected", equalTo("Yes"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }


}
