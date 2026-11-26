package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.DUPLICATE;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.INVALID;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.LINKED;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.NOT_READY;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.VALID;

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

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private ObjectMapper objectMapper;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/create-case-with-access-coderequest.json";
    private static final String ACCESS_CODE = "FVJKGHF";
    private static final String VALIDATE_ACCESS_CODE = "/citizen/validate-access-code";
    private static final String LINK_CASE = "/citizen/link-case-to-account";
    private static final String LINK_CASE_WITH_HEARING = "/citizen/link-case-to-account-with-hearing";

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
        caseDetails1 = createCase(requestBody);

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
        moveCaseToState(caseDetails1, State.DECISION_OUTCOME);
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
        CaseDetails caseDetails3 = createCase(requestBody);

        Assertions.assertNotNull(caseDetails3);
        Assertions.assertNotNull(caseDetails3.getId());
        moveCaseToState(caseDetails3, State.DECISION_OUTCOME);

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

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "ALL_FINAL_ORDERS_ISSUED"
    })
    @Order(6)
    void givenEligibleState_whenRespondentActivates_thenCaseIsLinked(State state) throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(false);
        moveCaseToState(caseDetails, state);
        JsonNode originalInvites = readCaseData(caseDetails).path("caseInvites");

        assertValidationStatus(caseDetails, ACCESS_CODE, VALID, idamTokenGenerator.generateIdamTokenForCitizen());
        Assertions.assertEquals(originalInvites, readCaseData(caseDetails).path("caseInvites"),
                                "Validating a code must not consume the invitation");

        citizenRequest(caseDetails)
            .post(LINK_CASE_WITH_HEARING)
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("caseData.caseInvites[0].value.hasLinked", equalTo("Yes"))
            .body("caseData.respondents[0].value.user.idamId", equalTo(citizenUserId()))
            .body("caseData.applicants[0].value.user.idamId", nullValue());

        assertStoredCitizenLink(caseDetails, false, state);
    }

    @Test
    @Order(7)
    void givenEligibleState_whenApplicantActivatesWithoutHearings_thenCaseIsLinked() throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(true);
        moveCaseToState(caseDetails, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);

        citizenRequest(caseDetails)
            .post(LINK_CASE)
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("caseInvites[0].value.hasLinked", equalTo("Yes"))
            .body("applicants[0].value.user.idamId", equalTo(citizenUserId()))
            .body("respondents[0].value.user.idamId", nullValue());

        assertStoredCitizenLink(caseDetails, true, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"AWAITING_INFORMATION", "SUBMITTED_NOT_PAID"})
    @Order(8)
    void givenIneligibleState_whenCitizenActivates_thenInvitationIsUnchanged(State state) throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(false);
        moveCaseToState(caseDetails, state);
        JsonNode originalInvites = readCaseData(caseDetails).path("caseInvites");

        assertValidationStatus(caseDetails, ACCESS_CODE, NOT_READY, idamTokenGenerator.generateIdamTokenForCitizen());
        assertActivationRejected(caseDetails);

        JsonNode storedData = readCaseData(caseDetails);
        Assertions.assertEquals(originalInvites, storedData.path("caseInvites"));
        Assertions.assertNull(storedData.path("respondents").get(0).path("value").path("user")
                                  .path("idamId").asText(null));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"AWAITING_INFORMATION", "PREPARE_FOR_HEARING_CONDUCT_HEARING"})
    @Order(9)
    void givenIncorrectCode_whenCitizenValidates_thenInvalidRegardlessOfState(State state) throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(false);
        moveCaseToState(caseDetails, state);
        JsonNode originalInvites = readCaseData(caseDetails).path("caseInvites");

        assertValidationStatus(caseDetails, "WRONGCODE", INVALID, idamTokenGenerator.generateIdamTokenForCitizen());

        Assertions.assertEquals(originalInvites, readCaseData(caseDetails).path("caseInvites"));
    }

    @Test
    @Order(10)
    void givenStateChangesAfterValidation_whenCitizenActivates_thenNotReady() throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(false);
        moveCaseToState(caseDetails, State.PREPARE_FOR_HEARING_CONDUCT_HEARING);
        assertValidationStatus(caseDetails, ACCESS_CODE, VALID, idamTokenGenerator.generateIdamTokenForCitizen());
        JsonNode originalInvites = readCaseData(caseDetails).path("caseInvites");

        moveCaseToState(caseDetails, State.AWAITING_INFORMATION);
        assertActivationRejected(caseDetails);

        Assertions.assertEquals(originalInvites, readCaseData(caseDetails).path("caseInvites"));
    }

    @Test
    @Order(11)
    void givenLinkedCodeInIneligibleState_whenValidated_thenExistingStatusesArePreserved() throws Exception {
        CaseDetails caseDetails = createCaseForCitizen(false);
        moveCaseToState(caseDetails, State.DECISION_OUTCOME);
        citizenRequest(caseDetails).post(LINK_CASE).then().statusCode(200);
        JsonNode linkedInvites = readCaseData(caseDetails).path("caseInvites");

        moveCaseToState(caseDetails, State.AWAITING_INFORMATION);
        assertValidationStatus(caseDetails, ACCESS_CODE, DUPLICATE, idamTokenGenerator.generateIdamTokenForCitizen());
        assertValidationStatus(caseDetails, ACCESS_CODE, LINKED, idamTokenGenerator.generateIdamTokenForSystem());

        Assertions.assertEquals(linkedInvites, readCaseData(caseDetails).path("caseInvites"));
    }

    private CaseDetails createCaseForCitizen(boolean isApplicant) throws Exception {
        Assertions.assertNotEquals(
            idamTokenGenerator.getUserDetailsFor(idamTokenGenerator.generateIdamTokenForSystem()).getId(),
            citizenUserId(), "The citizen activating the code must not be the case creator");
        ObjectNode request = (ObjectNode) objectMapper.readTree(
            ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY));
        JsonNode data = request.path("case_details").path("case_data");
        JsonNode party = data.path(isApplicant ? "applicants" : "respondents").get(0);
        ((ObjectNode) party.path("value")).putObject("user");
        ObjectNode invite = (ObjectNode) data.path("caseInvites").get(0).path("value");
        invite.put("partyId", party.path("id").asText());
        invite.put("isApplicant", isApplicant ? "Yes" : "No");
        invite.put("hasLinked", "No");
        invite.remove("invitedUserId");
        return createCase(objectMapper.writeValueAsString(request));
    }

    private CaseDetails createCase(String requestBody) {
        return newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(requestBody)
            .post("/testing-support/create-ccd-case-data")
            .then()
            .statusCode(200)
            .extract().as(CaseDetails.class);
    }

    private RequestSpecification citizenRequest(CaseDetails caseDetails) {
        return newRequest()
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(Map.of("caseId", caseDetails.getId().toString(), "accessCode", ACCESS_CODE, "hearingNeeded", "No"));
    }

    private void assertValidationStatus(CaseDetails caseDetails, String code, String status, String authorisation) {
        newRequest()
            .header("Authorization", authorisation)
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(Map.of("caseId", caseDetails.getId().toString(), "accessCode", code))
            .post(VALIDATE_ACCESS_CODE)
            .then()
            .statusCode(200)
            .body(equalTo(status));
    }

    private void assertActivationRejected(CaseDetails caseDetails) {
        for (String endpoint : new String[]{LINK_CASE, LINK_CASE_WITH_HEARING}) {
            citizenRequest(caseDetails)
                .post(endpoint)
                .then()
                .statusCode(409)
                .body(equalTo(NOT_READY));
        }
    }

    private JsonNode readCaseData(CaseDetails caseDetails) {
        return objectMapper.valueToTree(readCase(caseDetails).getData());
    }

    private CaseDetails readCase(CaseDetails caseDetails) {
        return coreCaseDataApi.getCase(
            idamTokenGenerator.generateIdamTokenForSystem(), serviceAuthenticationGenerator.generateTokenForCcd(),
            caseDetails.getId().toString());
    }

    private String citizenUserId() {
        return idamTokenGenerator.getUserDetailsFor(idamTokenGenerator.generateIdamTokenForCitizen()).getId();
    }

    private void assertStoredCitizenLink(CaseDetails caseDetails, boolean isApplicant, State expectedState) {
        CaseDetails storedCase = readCase(caseDetails);
        Assertions.assertEquals(expectedState.getValue(), storedCase.getState(),
                                "Activating an access code must not change the case state");
        JsonNode storedData = objectMapper.valueToTree(storedCase.getData());
        JsonNode invite = storedData.path("caseInvites").get(0).path("value");
        Assertions.assertEquals("Yes", invite.path("hasLinked").asText());
        Assertions.assertEquals(citizenUserId(), invite.path("invitedUserId").asText());
        JsonNode party = storedData.path(isApplicant ? "applicants" : "respondents").get(0);
        Assertions.assertEquals(party.path("id").asText(), invite.path("partyId").asText());
        Assertions.assertEquals(citizenUserId(), party.path("value").path("user").path("idamId").asText());
    }

    private void moveCaseToState(CaseDetails caseDetails, State state) {
        String authorisation = idamTokenGenerator.generateIdamTokenForSystem();
        String serviceAuthorisation = serviceAuthenticationGenerator.generateTokenForCcd();
        String userId = idamTokenGenerator.getUserDetailsFor(authorisation).getId();
        String caseId = caseDetails.getId().toString();
        StartEventResponse startEvent = coreCaseDataApi.startEventForCaseWorker(
            authorisation, serviceAuthorisation, userId, JURISDICTION, CASE_TYPE, caseId,
            CaseEvent.HMC_CASE_STATUS_UPDATE_TO_DECISION_OUTCOME.getValue()
        );
        CaseDataContent content = CaseDataContent.builder()
            .eventToken(startEvent.getToken())
            .event(Event.builder().id(startEvent.getEventId()).build())
            .data(Map.of("state", state.getValue(), "caseTypeOfApplication", "C100"))
            .build();
        CaseDetails updatedCase = coreCaseDataApi.submitEventForCaseWorker(
            authorisation, serviceAuthorisation, userId, JURISDICTION, CASE_TYPE, caseId, true, content
        );
        Assertions.assertEquals(state.getValue(), updatedCase.getState(),
                                "The test case must be in the requested state before activation");
    }
}
