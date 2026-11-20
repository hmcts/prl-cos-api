package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.NOT_READY;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private RequestSpecification newRequest() {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);
    }

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static final String CITIZEN_REQUEST_BODY
        = "requests/link-citizen-case-access-code.json";

    private static final String CITIZEN_REQUEST_BODY1
        = "requests/link-citizen-case-access-code1.json";

    @Test
    @Order(1)
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        caseDetails1 = newRequest()
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

        Assertions.assertNotNull(caseDetails1);
        Assertions.assertNotNull(caseDetails1.getId());
        Assertions.assertEquals(State.JUDICIAL_REVIEW.getValue(), caseDetails1.getState());

    }

    @Test
    @Order(2)
    public void givenValidCodeBeforeEligibleState_thenNotReadyResponse() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1)
            .replace("1711626009844772", caseDetails1.getId().toString());

        newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(requestBody)
            .post("/citizen/validate-access-code")
            .then()
            .statusCode(200)
            .body(equalTo(NOT_READY));

        for (String endpoint : new String[]{"/citizen/link-case-to-account", "/citizen/link-case-to-account-with-hearing"}) {
            newRequest()
                .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                .contentType("application/json")
                .body(requestBody)
                .post(endpoint)
                .then()
                .statusCode(409)
                .body(equalTo(NOT_READY));
        }
    }

    @Test
    @Order(3)
    public void givenRequestBody_linkCitizenToCaseWithHearing_then200Response() throws Exception {
        moveCaseToHearingOutcome(caseDetails1);
        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody
            .replace("1711626009844772", caseDetails1.getId().toString());

        CaseDataWithHearingResponse response = newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/link-case-to-account-with-hearing")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("caseData.caseInvites[0].id", equalTo("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
            .body("caseData.caseInvites[0].value.accessCode", equalTo("FVJKGHF"))
            .body("caseData.caseInvites[0].value.hasLinked", equalTo(YesOrNo.Yes.toString()))
            .extract()
            .as(CaseDataWithHearingResponse.class);
        Assertions.assertNotNull(response);
    }

    @Test
    @Order(4)
    public void givenRequestBody_validateAccessCode_thenDuplicateResponse() throws Exception {

        String requestBody = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody
            .replace("1711626009844772", caseDetails1.getId().toString());
        String response = newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/validate-access-code")
            .then()
            .statusCode(200)
            .extract()
            .asString();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Duplicate",response);
    }

    @Test
    @Order(5)
    public void givenRequestBody_validateAccessCode_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        CaseDetails caseDetails3 = newRequest()
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

        Assertions.assertNotNull(caseDetails3);
        Assertions.assertNotNull(caseDetails3.getId());
        moveCaseToHearingOutcome(caseDetails3);

        String requestBody1 = ResourceLoader.loadJson(CITIZEN_REQUEST_BODY1);
        String requestBodyRevised = requestBody1
            .replace("1711626009844772", caseDetails3.getId().toString());
        String response = newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/citizen/validate-access-code")
            .then()
            .statusCode(200)
            .extract()
            .asString();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Valid",response);
    }

    private void moveCaseToHearingOutcome(CaseDetails caseDetails) {
        // Test-support creation always leaves the case in Judicial Review; use CCD's HMC event to change its state.
        newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(Map.of("caseRef", caseDetails.getId().toString(),
                         "hearingUpdate", Map.of("hmcStatus", "COMPLETED")))
            .pathParam("caseState", State.DECISION_OUTCOME.getValue())
            .put("/hearing-management-state-update/{caseState}")
            .then()
            .statusCode(200);
    }

}
