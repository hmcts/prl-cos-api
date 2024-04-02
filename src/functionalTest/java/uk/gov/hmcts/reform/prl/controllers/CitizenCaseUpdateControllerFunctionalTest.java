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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/update-case-event-citizen-request.json";

    private static CaseDetails caseDetails1;

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    private static final String CITIZEN_UPDATE_CASE_REQUEST_BODY
        = "requests/citizen-update-case.json";


    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY);
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
        Assert.assertNotNull(caseDetails1.getId());

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

        CaseData response = request1
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

        System.out.println("MMMM " + response);
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


}
