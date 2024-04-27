package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CitizenCaseUpdateControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    public static final String AUTHORIZATION = "Authorization";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String CASE_ID = "caseId";

    public static final String EVENT_ID = "eventId";

    public static final String updatePartyDetailsEndPoint = "/citizen/{caseId}/{eventId}/update-party-details";

    public static final String saveDraftCitizenApplicationEndPoint = "/citizen/{caseId}/save-c100-draft-application";

    public static final String deleteApplicationCitizenEndPoint = "/citizen/{caseId}/delete-application";

    public static final String withDrawCaseCitizenEndPoint = "/citizen/{caseId}/withdraw";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/update-case-event-citizen-request.json";

    private static CaseDetails caseDetails1;

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request2 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    private static final String CITIZEN_UPDATE_CASE_REQUEST_BODY
        = "requests/citizen-update-case.json";

    private static final String SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY = "requests/save-c100-draft-citizen.json";

    private static final String DELETE_APPLICATION_CITIZEN_REQUEST_BODY = "requests/delete-aplication-citizen.json";

    private static final String WITHDRAW_APPLICATION_CITIZEN_REQUEST_BODY = "requests/withdraw-aplication-citizen.json";

    private static final String SUBMITTED_READY_FOR_WITHDRAW_REQUEST_BODY = "requests/submitted-aplication-ready-for-withdraw.json";


    @Test
    public void createCcdTestCase() throws Exception {

        /*String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        caseDetails1 =  request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails1);
        Assert.assertNotNull(caseDetails1.getId());*/


        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
        /*caseDetails1 =  request1
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

        Assert.assertNotNull(caseDetails1);
        Assert.assertNotNull(caseDetails1.getId());*/

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

        //assertTrue(json.contains("welshLanguageRequirementsTable"));


    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_confirmYourDetails_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"confirmYourDetails")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.email", equalTo("citizen@email.com"),
                  "applicants[0].value.phoneNumber", equalTo("07442772347"),
                  "applicants[0].value.placeOfBirth", equalTo("Harrow"),
                  "applicants[0].value.dateOfBirth", equalTo("1997-12-12"))
            .extract()
            .as(CaseData.class);
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_keepYourDetailsPrivate_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"keepYourDetailsPrivate")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.keepDetailsPrivate.otherPeopleKnowYourContactDetails", equalTo("yes"),
                  "applicants[0].value.response.keepDetailsPrivate.confidentiality", equalTo("Yes"),
                  "applicants[0].value.response.keepDetailsPrivate.confidentialityList[0]", equalTo("phoneNumber"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_consentToTheApplication_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"consentToTheApplication")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.consent.consentToTheApplication", equalTo("Yes"),
                  "applicants[0].value.response.consent.applicationReceivedDate", equalTo("2023-01-23"),
                  "applicants[0].value.response.consent.permissionFromCourt", equalTo("Yes"),
                  "applicants[0].value.response.consent.courtOrderDetails", equalTo("Court Order details test"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_respondentMiam_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        /*request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"respondentMiam")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.miam.attendedMiam", equalTo("Yes"),
                  "applicants[0].value.response.miam.willingToAttendMiam", equalTo("Yes"),
                  "applicants[0].value.response.miam.reasonNotAttendingMiam", equalTo("No reason"))
            .extract()
            .as(CaseData.class);*/
        String url = "/citizen/" + caseDetails1.getId().toString() + "/respondentMiam/update-party-details";
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.miam.attendedMiam").value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.miam.willingToAttendMiam").value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.miam.reasonNotAttendingMiam").value("No reason"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_legalRepresentation_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"legalRepresentation")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.legalRepresentation", equalTo("Yes"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenAoH_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        /*request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizenRespondentAoH")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.safetyConcerns.child.physicalAbuse.behaviourDetails", equalTo("behaviour was not acceptable"),
                  "applicants[0].value.response.safetyConcerns.child.physicalAbuse.behaviourStartDate", equalTo("2023-07-07"),
                  "applicants[0].value.response.safetyConcerns.child.physicalAbuse.isOngoingBehaviour", equalTo("Yes"),
                  "applicants[0].value.response.safetyConcerns.child.physicalAbuse.seekHelpFromPersonOrAgency", equalTo("Yes"))
            .extract()
            .as(CaseData.class);*/

        String url = "/citizen/" + caseDetails1.getId().toString() + "/citizenRespondentAoH/update-party-details";
        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.safetyConcerns.child.physicalAbuse.behaviourDetails")
                           .value("behaviour was not acceptable"))
            .andExpect(jsonPath("applicants[0].value.response.safetyConcerns.child.physicalAbuse.behaviourStartDate")
                           .value("2023-07-07"))
            .andExpect(jsonPath("applicants[0].value.response.safetyConcerns.child.physicalAbuse.isOngoingBehaviour")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.safetyConcerns.child.physicalAbuse.seekHelpFromPersonOrAgency")
                           .value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenInternationalElement_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizenInternationalElement")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.citizenInternationalElements.childrenLiveOutsideOfEnWl", equalTo("Yes"),
                  "applicants[0].value.response.citizenInternationalElements.childrenLiveOutsideOfEnWlDetails", equalTo("some children live outside"),
                  "applicants[0].value.response.citizenInternationalElements.parentsAnyOneLiveOutsideEnWl", equalTo("Yes"),
                  "applicants[0].value.response.citizenInternationalElements.parentsAnyOneLiveOutsideEnWlDetails", equalTo("Living outside EnWl"))
            .extract()
            .as(CaseData.class);
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenRemoveLegalRepresentative_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizenRemoveLegalRepresentative")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.isRemoveLegalRepresentativeRequested", equalTo("Yes"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_hearingNeeds_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"hearingNeeds")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.supportYouNeed.helpCommunication[0]", equalTo("hearingloop"),
                  "applicants[0].value.response.supportYouNeed.courtComfort[0]", equalTo("appropriatelighting"),
                  "applicants[0].value.response.supportYouNeed.courtHearing[0]", equalTo("supportworker"),
                  "applicants[0].value.response.supportYouNeed.parkingDetails", equalTo("Need space for parking"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    @Ignore
    public void givenRequestBody_updateCitizenParty_Event_citizenContactPreference_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizenContactPreference")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.contactPreferences", equalTo("digital"))
            .extract()
            .as(CaseData.class);

    }

    @Test
    @Ignore
    public void givenRequestBody_updateCitizenParty_Event_citizenInternalFlagUpdates_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizenInternalFlagUpdates")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.citizenFlags.isApplicationViewed", equalTo("Yes"),
                  "applicants[0].value.response.citizenFlags.isAllegationOfHarmViewed", equalTo("No"),
                  "applicants[0].value.response.citizenFlags.isAllDocumentsViewed", equalTo("Yes"))
            .extract()
            .as(CaseData.class);
    }


    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizen_case_update_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

        request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,caseDetails1.getId().toString())
            .pathParam(EVENT_ID,"citizen-case-update")
            .post(updatePartyDetailsEndPoint)
            .then()
            .body("applicants[0].value.response.currentOrPreviousProceedings.haveChildrenBeenInvolvedInCourtCase", equalTo("Yes"))
            .body("applicants[0].value.response.currentOrPreviousProceedings.courtOrderMadeForProtection", equalTo("Yes"))
            .extract()
            .as(CaseData.class);
    }

    @Test
    @Ignore
    public void givenRequestBody_saveDraftCitizenApplication_then200Response() throws Exception {

        CaseData createNewCase = request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/testing-support/create-dummy-citizen-case")
            .then()
            .extract()
            .as(CaseData.class);
        Assert.assertNotNull(createNewCase);
        Assert.assertNotNull(createNewCase.getId());

        String requestBody = ResourceLoader.loadJson(SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY);

        String requestBodyRevised = requestBody.replace("1712061560509233", String.valueOf(createNewCase.getId()));

        CaseData saveedCaseData = request2
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,String.valueOf(createNewCase.getId()))
            .post(saveDraftCitizenApplicationEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assert.assertNotNull(createNewCase);
        Assert.assertNotNull(saveedCaseData);

        JSONObject createCaseMiamResponse = new JSONObject(createNewCase.getC100RebuildData().getC100RebuildMaim());
        JSONObject savedMiamResponse = new JSONObject(saveedCaseData.getC100RebuildData().getC100RebuildMaim());

        Assert.assertEquals(YesOrNo.Yes.toString(), createCaseMiamResponse.get("miam_consent"));
        Assert.assertEquals(YesOrNo.No.toString(),savedMiamResponse.get("miam_consent"));

        JSONObject createCaseHwfResponse = new JSONObject(createNewCase.getC100RebuildData().getC100RebuildHelpWithFeesDetails());
        JSONObject savedHwfResponse = new JSONObject(saveedCaseData.getC100RebuildData().getC100RebuildHelpWithFeesDetails());

        Assert.assertEquals(YesOrNo.No.toString(),createCaseHwfResponse.get("hwf_needHelpWithFees"));
        Assert.assertEquals(YesOrNo.Yes.toString(),savedHwfResponse.get("hwf_needHelpWithFees"));

    }


    @Test
    @Ignore
    public void givenRequestBody_deleteApplicationCitizen_then200Response() throws Exception {

        CaseData createNewCase = request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/testing-support/create-dummy-citizen-case")
            .then()
            .extract()
            .as(CaseData.class);
        Assert.assertNotNull(createNewCase);
        Assert.assertNotNull(createNewCase.getId());

        String requestBody = ResourceLoader.loadJson(DELETE_APPLICATION_CITIZEN_REQUEST_BODY);

        String requestBodyRevised = requestBody.replace("1712061560509233", String.valueOf(createNewCase.getId()));

        CaseData deletedApplicationCaseData = request2
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID, createNewCase.getId())
            .post(deleteApplicationCitizenEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assert.assertNotNull(createNewCase);
        Assert.assertNotNull(deletedApplicationCaseData);

    }

    @Test
    public void givenRequestBody_withdrawCase_then200Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(SUBMITTED_READY_FOR_WITHDRAW_REQUEST_BODY);
        CaseDetails caseDetails =  request1
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());

        String requestBody1 = ResourceLoader.loadJson(WITHDRAW_APPLICATION_CITIZEN_REQUEST_BODY);

        CaseData withDrawCase = request2
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody1)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam(CASE_ID,String.valueOf(caseDetails.getId()))
            .post(withDrawCaseCitizenEndPoint)
            .then()
            .extract()
            .as(CaseData.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(withDrawCase);

    }
}
