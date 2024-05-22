package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRUE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CallbackControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    protected CaseEventService caseEventService;

    private final String userToken = "Bearer testToken";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
    private static final String FL401_VALID_REQUEST_BODY = "requests/fl401-add-case-number.json";
    private static final String MIAM_VALIDATION_REQUEST_ERROR = "requests/call-back-controller-miam-request-error.json";
    private static final String MIAM_VALIDATION_REQUEST_NO_ERROR = "requests/call-back-controller-miam-request-no-error.json";
    private static final String APPLICANT_CASE_NAME_REQUEST = "requests/call-back-controller-applicant-case-name.json";
    private static final String APPLICATION_TIMETABLE_REQUEST = "requests/call-back-controller-validate-application-timeframe-error.json";
    private static final String C100_APPLICANT_DETAILS = "requests/call-back-controller-C100-case-data.json";
    private static final String FL401_APPLICANT_DETAILS = "requests/call-back-controller-fl401-case-data.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenNoMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndMiamError() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);

        mockMvc.perform(post("/validate-miam-application-or-exemption")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_NO_ERROR);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-miam-application-or-exemption")
            .then()
            .body("errors", Matchers.hasSize(0))
            .assertThat().statusCode(200);
    }


    @Test
    @Ignore
    public void givenNoAuthorization_whenPostRequestToDraftDocumentGeneration_then400Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-save-draft-document")
            .then().assertThat().statusCode(200);
    }


    @Test
    public void givenRequestWithApplicantOrRespondentCaseName_whenEndPointCalled_ResponseContainsApplicantCaseName() throws Exception {
        String requestBody = ResourceLoader.loadJson(APPLICANT_CASE_NAME_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/about-to-submit-case-creation")
            .then()
            .body("data.applicantCaseName", equalTo("Test Name"))
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequest_whenEndPointCalled_ResponseContainsApplicantCaseName() throws Exception {
        String requestBody = ResourceLoader.loadJson(APPLICATION_TIMETABLE_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-application-consideration-timetable")
            .then()
            .body("errors", contains("Please provide either days or hours in proposed timetable"))
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequest_whenEndPointCalled_ResponseContains() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/copy-manage-docs-for-tabs")
            .then()
            .body("data.furtherEvidences", nullValue())
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestWithC100ApplicantDetails_whenEndPointCalled_ResponseContainsTypeOfApplication() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-withdrawn-about-to-submit")
            .then()
            .assertThat().statusCode(500);
    }

    @Test
    public void givenRequestWithCaseNumberAdded_ResponseContainsIssueDate() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_VALID_REQUEST_BODY);
        mockMvc.perform(post("/fl401-add-case-number")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestWithApplicantOrRespondentCaseName_whenEndPointCalled_ResponseContainsCaseNameHmctsInternal() throws Exception {
        String requestBody = ResourceLoader.loadJson(APPLICANT_CASE_NAME_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/about-to-submit-case-creation")
            .then()
            .body("data.caseNameHmctsInternal", equalTo("Test Name"))
            .assertThat().statusCode(200);
    }

    @Test
    public void testAttachScanDocsWaChange() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/attach-scan-docs/about-to-submit")
            .then()
            .body("data.manageDocumentsRestrictedFlag", equalTo(TRUE),
                  "data.manageDocumentsTriggeredBy", equalTo(STAFF))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

}
