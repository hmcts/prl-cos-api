package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.Objects;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class CitizenCaseUpdateControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    public static final String CASE_ID = "caseId";

    public static final String EVENT_ID = "eventId";

    public static final String updatePartyDetailsEndPoint = "/citizen/{caseId}/{eventId}/update-party-details";

    public static final String saveDraftCitizenApplicationEndPoint = "/citizen/{caseId}/save-c100-draft-application";

    public static final String submitC100ApplicationEndPoint = "/citizen/{caseId}/{eventId}/submit-c100-application";

    public static final String deleteApplicationCitizenEndPoint = "/citizen/{caseId}/delete-application";

    public static final String withDrawCaseCitizenEndPoint = "/citizen/{caseId}/withdraw";

    private static final String CREATE_DUMMY_CITIZEN_CASE = "/testing-support/create-dummy-citizen-case";

    private static final String CREATE_CCD_CASE = "/testing-support/create-ccd-case-data";

    private static final String DUMMY_CASE_ID = "1712061560509233";

    private final String targetInstance = StringUtils.stripEnd(
        StringUtils.defaultIfBlank(System.getenv("TEST_URL"), "http://localhost:4044"), "/");

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/update-case-event-citizen-request.json";

    private static final String CITIZEN_UPDATE_CASE_REQUEST_BODY = "requests/citizen-update-case.json";

    private static final String SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY = "requests/save-c100-draft-citizen.json";

    private static final String DELETE_APPLICATION_CITIZEN_REQUEST_BODY = "requests/delete-aplication-citizen.json";

    private static final String WITHDRAW_APPLICATION_CITIZEN_REQUEST_BODY = "requests/withdraw-aplication-citizen.json";

    private static final String SUBMITTED_READY_FOR_WITHDRAW_REQUEST_BODY = "requests/submitted-aplication-ready-for-withdraw.json";

    /**
     * Returns a fresh RestAssured request specification for every HTTP call. RestAssured
     * specifications are mutable/stateful, so reusing a single shared instance across calls
     * accumulates headers/body and produces malformed requests (rejected with 400 at the gateway).
     */
    private RequestSpecification newRequest() {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);
    }

    /**
     * Creates a fresh CCD case for a single test so the update-party tests stay independent.
     */
    private CaseDetails createCcdTestCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        CaseDetails caseDetails = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_CCD_CASE)
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Objects.requireNonNull(caseDetails, "CCD test case was not created");
        Objects.requireNonNull(caseDetails.getId(), "CCD test case has no id");
        return caseDetails;
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_confirmYourDetails_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "confirmYourDetails")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.email", equalTo("citizen@email.com"),
                  "caseData.applicants[0].value.phoneNumber", equalTo("07442772347"),
                  "caseData.applicants[0].value.placeOfBirth", equalTo("Harrow"),
                  "caseData.applicants[0].value.dateOfBirth", equalTo("1997-12-12"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_keepYourDetailsPrivate_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "keepYourDetailsPrivate")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.response.keepDetailsPrivate.otherPeopleKnowYourContactDetails", equalTo("yes"),
                  "caseData.applicants[0].value.response.keepDetailsPrivate.confidentiality", equalTo("Yes"),
                  "caseData.applicants[0].value.response.keepDetailsPrivate.confidentialityList[0]", equalTo("phoneNumber"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_consentToTheApplication_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "consentToTheApplication")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.response.consent.consentToTheApplication", equalTo("Yes"),
                  "caseData.applicants[0].value.response.consent.applicationReceivedDate", equalTo("2023-01-23"),
                  "caseData.applicants[0].value.response.consent.permissionFromCourt", equalTo("Yes"),
                  "caseData.applicants[0].value.response.consent.courtOrderDetails", equalTo("Court Order details test"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_respondentMiam_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "respondentMiam")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.response.miam.attendedMiam", equalTo("Yes"),
                  "caseData.applicants[0].value.response.miam.willingToAttendMiam", equalTo("Yes"),
                  "caseData.applicants[0].value.response.miam.reasonNotAttendingMiam", equalTo("No reason"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_legalRepresentation_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "legalRepresentation")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.response.legalRepresentation", equalTo("Yes"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenAoH_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenRespondentAoH")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.response.respAohYesOrNo", equalTo("No"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenInternationalElement_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String intlElements = "caseData.applicants[0].value.response.citizenInternationalElements";

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenInternationalElement")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body(intlElements + ".childrenLiveOutsideOfEnWl", equalTo("Yes"),
                  intlElements + ".childrenLiveOutsideOfEnWlDetails", equalTo("some children live outside"),
                  intlElements + ".parentsAnyOneLiveOutsideEnWl", equalTo("Yes"),
                  intlElements + ".parentsAnyOneLiveOutsideEnWlDetails", equalTo("Living outside EnWl"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenRemoveLegalRepresentative_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenRemoveLegalRepresentative")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.isRemoveLegalRepresentativeRequested", equalTo("Yes"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_hearingNeeds_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String supportYouNeed = "caseData.applicants[0].value.response.supportYouNeed";

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "hearingNeeds")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body(supportYouNeed + ".helpCommunication[0]", equalTo("hearingloop"),
                  supportYouNeed + ".courtComfort[0]", equalTo("appropriatelighting"),
                  supportYouNeed + ".courtHearing[0]", equalTo("supportworker"),
                  supportYouNeed + ".parkingDetails", equalTo("Need space for parking"));
    }

    @Test
    @Disabled
    public void givenRequestBody_updateCitizenParty_Event_citizenContactPreference_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenContactPreference")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body("caseData.applicants[0].value.contactPreferences", equalTo("email"));
    }

    @Test
    @Disabled
    public void givenRequestBody_updateCitizenParty_Event_citizenInternalFlagUpdates_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String citizenFlags = "caseData.applicants[0].value.response.citizenFlags";

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenInternalFlagUpdates")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body(citizenFlags + ".isApplicationViewed", equalTo("Yes"),
                  citizenFlags + ".isAllegationOfHarmViewed", equalTo("No"),
                  citizenFlags + ".isAllDocumentsViewed", equalTo("Yes"));
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenCurrentOrPreviousProceeding_then200Response() throws Exception {
        CaseDetails caseDetails1 = createCcdTestCase();
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String proceedings = "caseData.applicants[0].value.response.currentOrPreviousProceedings";

        newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, caseDetails1.getId().toString())
            .pathParam(EVENT_ID, "citizenCurrentOrPreviousProceeding")
            .post(updatePartyDetailsEndPoint)
            .then()
            .assertThat().statusCode(200)
            .body(proceedings + ".haveChildrenBeenInvolvedInCourtCase", equalTo("Yes"))
            .body(proceedings + ".courtOrderMadeForProtection", equalTo("Yes"));
    }

    @Test
    public void givenRequestBody_saveDraftCitizenApplication_then200Response() throws Exception {

        CaseData createNewCase = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_DUMMY_CITIZEN_CASE)
            .then()
            .extract()
            .as(CaseData.class);
        Assertions.assertNotNull(createNewCase);
        Assertions.assertTrue(createNewCase.getId() > 0);

        String requestBody = ResourceLoader.loadJson(SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY);

        // The request file has no help-with-fees block, but this test asserts it is saved as "Yes".
        JSONObject saveBody = new JSONObject(requestBody.replace(DUMMY_CASE_ID, String.valueOf(createNewCase.getId())));
        saveBody.put("c100RebuildHelpWithFeesDetails", "{\"hwf_needHelpWithFees\":\"Yes\"}");
        String requestBodyRevised = saveBody.toString();

        CaseData saveedCaseData = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, String.valueOf(createNewCase.getId()))
            .post(saveDraftCitizenApplicationEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assertions.assertNotNull(createNewCase);
        Assertions.assertNotNull(saveedCaseData);

        JSONObject createCaseMiamResponse = new JSONObject(createNewCase.getC100RebuildData().getC100RebuildMaim());
        JSONObject savedMiamResponse = new JSONObject(saveedCaseData.getC100RebuildData().getC100RebuildMaim());

        Assertions.assertEquals(YesOrNo.Yes.toString(), createCaseMiamResponse.get("miam_consent"));
        Assertions.assertEquals(YesOrNo.No.toString(), savedMiamResponse.get("miam_consent"));

        JSONObject createCaseHwfResponse = new JSONObject(createNewCase.getC100RebuildData().getC100RebuildHelpWithFeesDetails());
        JSONObject savedHwfResponse = new JSONObject(saveedCaseData.getC100RebuildData().getC100RebuildHelpWithFeesDetails());

        Assertions.assertEquals(YesOrNo.No.toString(), createCaseHwfResponse.get("hwf_needHelpWithFees"));
        Assertions.assertEquals(YesOrNo.Yes.toString(), savedHwfResponse.get("hwf_needHelpWithFees"));

    }

    @Test
    public void givenRequestBody_deleteApplicationCitizen_then200Response() throws Exception {

        CaseData createNewCase = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_DUMMY_CITIZEN_CASE)
            .then()
            .extract()
            .as(CaseData.class);
        Assertions.assertNotNull(createNewCase);
        Assertions.assertTrue(createNewCase.getId() > 0);

        String requestBody = ResourceLoader.loadJson(DELETE_APPLICATION_CITIZEN_REQUEST_BODY);

        String requestBodyRevised = requestBody.replace(DUMMY_CASE_ID, String.valueOf(createNewCase.getId()));

        CaseData deletedApplicationCaseData = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, createNewCase.getId())
            .post(deleteApplicationCitizenEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assertions.assertNotNull(createNewCase);
        Assertions.assertNotNull(deletedApplicationCaseData);

    }

    @Test
    public void givenRequestBody_withdrawCase_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(SUBMITTED_READY_FOR_WITHDRAW_REQUEST_BODY);
        CaseDetails caseDetails = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_CCD_CASE)
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assertions.assertNotNull(caseDetails);
        Assertions.assertNotNull(caseDetails.getId());

        String requestBody1 = ResourceLoader.loadJson(WITHDRAW_APPLICATION_CITIZEN_REQUEST_BODY);

        CaseData withDrawCase = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody1)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, String.valueOf(caseDetails.getId()))
            .post(withDrawCaseCitizenEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assertions.assertNotNull(caseDetails);
        Assertions.assertNotNull(withDrawCase);

    }

    @Test
    public void givenRequestBody_submitC100Application_then200Response() throws Exception {

        CaseData createNewCase = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_DUMMY_CITIZEN_CASE)
            .then()
            .extract()
            .as(CaseData.class);
        Assertions.assertNotNull(createNewCase);
        Assertions.assertTrue(createNewCase.getId() > 0);

        String requestBody = ResourceLoader.loadJson(SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY);
        // The save-draft body is not a complete submit payload. The citizen frontend always sends these
        // blocks on submit (consent order is always present, as "{}" when empty), so add them here.
        JSONObject submitBody = new JSONObject(requestBody.replace(DUMMY_CASE_ID, String.valueOf(createNewCase.getId())));
        submitBody.put("c100RebuildScreeningQuestions",
                       "{\"sq_writtenAgreement\":\"No\",\"sq_alternativeRoutes\":\"No\","
                           + "\"sq_legalRepresentation\":\"No\",\"sq_courtPermissionRequired\":\"No\"}");
        submitBody.put("c100RebuildHelpWithFeesDetails", "{\"hwf_needHelpWithFees\":\"No\"}");
        submitBody.put("c100RebuildConsentOrderDetails", "{}");
        String requestBodyRevised = submitBody.toString();

        // Use the citizen token (the user who created the case), as the real citizen frontend does.
        CaseData submittedCaseData = newRequest()
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, String.valueOf(createNewCase.getId()))
            .pathParam(EVENT_ID, "citizen-case-submit")
            .post(submitC100ApplicationEndPoint)
            .then()
            // Prints status, headers and full body whenever the call fails, so the
            // underlying CCD error surfaces in the local test output.
            .log().ifError()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseData.class);

        Assertions.assertNotNull(submittedCaseData);
        Assertions.assertEquals(createNewCase.getId(), submittedCaseData.getId());
    }

    @Test
    public void givenNoAuthHeaders_submitC100Application_thenUnauthorised() throws Exception {

        String requestBody = ResourceLoader.loadJson(SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY);

        newRequest()
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, DUMMY_CASE_ID)
            .pathParam(EVENT_ID, "citizen-case-submit")
            .post(submitC100ApplicationEndPoint)
            .then()
            .assertThat().statusCode(anyOf(is(400), is(401), is(403)));
    }
}

