package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.RETURNED_TO_ADMIN_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfidentialityCheckControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    private static final String VALID_REQUEST_BODY_WA1 = "requests/service-of-application-WA1.json";

    private static final String VALID_REQUEST_BODY_WA2 = "requests/service-of-application-WA2.json";

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
    private static CaseDetails caseDetails;
    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);
    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

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
    public void givenRequestWithCaseData_ResponseContainsNo() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
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
        assertTrue(json.contains(RETURNED_TO_ADMIN_HEADER));
    }

    @Test
    public void givenRequestBody_whenConfidentialCheckWhenAppplicationServedAndUnServedRespondentPackAvailable() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA1);

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
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA1);

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
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA1);

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
                  "data.responsibleForService", equalTo("applicantLegalRepresentative"),
                  "data.isC8CheckNeeded", equalTo("Yes"),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventC100WhenBothIsC8DocPresentAndSoaServeToRespondentOptionsYes() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA1);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"isAddressConfidential\": \"Yes\"",
                     "\"isAddressConfidential\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo("applicantLegalRepresentative"),
                  "data.isC8CheckNeeded", equalTo("Yes"),
                  "data.isOccupationOrderSelected", equalTo(null))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventC100WhenIsConfidentialC8DocAndSoaServeToRespondentOptionsNo() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA2);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"soaServeToRespondentOptions\": \"Yes\"",
                     "\"soaServeToRespondentOptions\": \"No\"")
            .replace("\"isConfidential\": \"Yes\"",
                     "\"isConfidential\": \"No\"")
            .replace("\"isAddressConfidential\": \"Yes\"",
                     "\"isAddressConfidential\": \"No\"");

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
    public void givenRequestWithCaseData_ResponseContainsYes() throws Exception {

        String requestBody = ResourceLoader.loadJson("requests/service-of-application-ready-to-serve.json");

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
    }

    @Test
    public void givenRequestBody_whenSoaEventFl401WhenBothIsConfidentialAndSoaServingRespondentsOptionsDaYes() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA1);

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
                  "data.responsibleForService", equalTo("courtBailiff"),
                  "data.isC8CheckNeeded", equalTo("Yes"),
                  "data.isOccupationOrderSelected", equalTo("Yes"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_whenSoaEventFL401WhenIsConfidentialNoAndC8DocNotPresent() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WA2);

        String requestBodyRevised = requestBody
            .replace("\"event_id\": \"litigationCapacity\"",
                     "\"event_id\": \"serviceOfApplication\"")
            .replace("\"caseTypeOfApplication\": \"C100\"",
                     "\"caseTypeOfApplication\": \"FL401\"")
            .replace("\"caseCreatedBy\": \"SOLICITOR\"",
                     "\"caseCreatedBy\": \"CITIZEN\"")
            .replace("\"isAddressConfidential\": \"Yes\"",
                     "\"isAddressConfidential\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/confidentiality-check/about-to-submit")
            .then()
            .body("data.isC8CheckApproved", equalTo(null),
                  "data.responsibleForService", equalTo("courtBailiff"),
                  "data.isC8CheckNeeded", equalTo("No"),
                  "data.isOccupationOrderSelected", equalTo("Yes"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

}
