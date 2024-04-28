package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
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
        RestAssured.registerParser("text/html", Parser.JSON);
    }


    private static final String CITIZEN_UPDATE_CASE_REQUEST_BODY
        = "requests/citizen-update-case.json";

    private static final String SAVE_C100_DRAFT_CITIZEN_REQUEST_BODY = "requests/save-c100-draft-citizen.json";

    private static final String DELETE_APPLICATION_CITIZEN_REQUEST_BODY = "requests/delete-aplication-citizen.json";

    private static final String WITHDRAW_APPLICATION_CITIZEN_REQUEST_BODY = "requests/withdraw-aplication-citizen.json";

    private static final String SUBMITTED_READY_FOR_WITHDRAW_REQUEST_BODY = "requests/submitted-aplication-ready-for-withdraw.json";


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
    public void givenRequestBody_updateCitizenParty_Event_confirmYourDetails_then200Response() throws Exception {

        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);


        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/confirmYourDetails/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
                            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.email").value("citizen@email.com"))
            .andExpect(jsonPath("applicants[0].value.phoneNumber").value("07442772347"))
            .andExpect(jsonPath("applicants[0].value.placeOfBirth").value("Harrow"))
            .andExpect(jsonPath("applicants[0].value.dateOfBirth").value("1997-12-12"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_keepYourDetailsPrivate_then200Response() throws Exception {

        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);


        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/keepYourDetailsPrivate/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
                            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.keepDetailsPrivate.otherPeopleKnowYourContactDetails")
                           .value("yes"))
            .andExpect(jsonPath("applicants[0].value.response.keepDetailsPrivate.confidentiality")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.keepDetailsPrivate.confidentialityList[0]")
                           .value("phoneNumber"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_consentToTheApplication_then200Response() throws Exception {
        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/consentToTheApplication/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.consent.consentToTheApplication")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.consent.applicationReceivedDate")
                           .value("2023-01-23"))
            .andExpect(jsonPath("applicants[0].value.response.consent.permissionFromCourt")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.consent.courtOrderDetails")
                           .value("Court Order details test"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_respondentMiam_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

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
        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/legalRepresentation/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.legalRepresentation")
                           .value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenAoH_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);

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
        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);


        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/citizenInternationalElement/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.citizenInternationalElements.childrenLiveOutsideOfEnWl")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.citizenInternationalElements.childrenLiveOutsideOfEnWlDetails")
                           .value("some children live outside"))
            .andExpect(jsonPath("applicants[0].value.response.citizenInternationalElements.parentsAnyOneLiveOutsideEnWl")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.citizenInternationalElements.parentsAnyOneLiveOutsideEnWlDetails")
                           .value("Living outside EnWl"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_citizenRemoveLegalRepresentative_then200Response() throws Exception {
        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/citizenRemoveLegalRepresentative/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.isRemoveLegalRepresentativeRequested")
                           .value("Yes"))
            .andReturn();
    }

    @Test
    public void givenRequestBody_updateCitizenParty_Event_hearingNeeds_then200Response() throws Exception {

        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/hearingNeeds/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.supportYouNeed.helpCommunication[0]")
                           .value("hearingloop"))
            .andExpect(jsonPath("applicants[0].value.response.supportYouNeed.courtComfort[0]")
                           .value("appropriatelighting"))
            .andExpect(jsonPath("applicants[0].value.response.supportYouNeed.courtHearing[0]")
                           .value("supportworker"))
            .andExpect(jsonPath("applicants[0].value.response.supportYouNeed.parkingDetails")
                           .value("Need space for parking"))
            .andReturn();
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

        String requestBodyCreate = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBodyCreate)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        CaseDetails caseDetails = mapper.readValue(json, CaseDetails.class);

        String requestBody = ResourceLoader.loadJson(CITIZEN_UPDATE_CASE_REQUEST_BODY);
        String url = "/citizen/" + caseDetails.getId().toString() + "/citizen-case-update/update-party-details";

        mockMvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("applicants[0].value.response.currentOrPreviousProceedings.haveChildrenBeenInvolvedInCourtCase")
                           .value("Yes"))
            .andExpect(jsonPath("applicants[0].value.response.currentOrPreviousProceedings.courtOrderMadeForProtection")
                           .value("Yes"))
            .andReturn();
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
