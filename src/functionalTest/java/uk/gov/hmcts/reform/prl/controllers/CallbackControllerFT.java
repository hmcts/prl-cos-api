package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRUE;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;

@SpringBootTest
@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
@Slf4j
public class CallbackControllerFT {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorisationService authorisationService;


    private static final String MIAM_VALIDATION_REQUEST_ERROR = "requests/call-back-controller-miam-request-error.json";
    private static final String MIAM_VALIDATION_REQUEST_NO_ERROR = "requests/call-back-controller-miam-request-no-error.json";
    private static final String C100_GENERATE_DRAFT_DOC = "requests/call-back-controller-generate-save-doc.json";
    private static final String C100_ISSUE_AND_SEND = "requests/call-back-controller-issue-and-send-to-local-court.json";
    private static final String C100_UPDATE_APPLICATION = "requests/call-back-controller-update-application.json";
    private static final String C100_WITHDRAW_APPLICATION = "requests/call-back-controller-withdraw-application.json";
    private static final String C100_SEND_TO_GATEKEEPER = "requests/call-back-controller-send-to-gatekeeper.json";
    private static final String C100_RESEND_RPA = "requests/call-back-controller-resend-rpa.json";
    private static final String FL401_ABOUT_TO_SUBMIT_CREATION = "requests/call-back-controller-about-to-submit-case-creation.json";
    private static final String FL401_CASE_DATA = "requests/call-back-controller-fl401-case-data.json";
    private static final String C100_SEND_TO_GATEKEEPERJUDGE = "requests/call-back-controller-send-to-gatekeeperForJudge.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static Long preCreatedCaseId;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new JavaTimeModule());
        if (preCreatedCaseId == null) {
            try {
                preCreatedCaseId = createCcdTestCase();
            } catch (Exception e) {
                log.error("Error while creating Ccd Case", e);
            }
        }
    }

    public Long createCcdTestCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        return request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class).getId();
    }

    @Test
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_NO_ERROR);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/validate-miam-application-or-exemption")
            .then()
            .assertThat().statusCode(200)
            .body(
                "errors[0]", nullValue(),
                "warnings",  nullValue()
            );
    }

    @Test
    public void givenNoMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndMiamError() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);

        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/validate-miam-application-or-exemption")
            .then()
            .assertThat().statusCode(200)
            .body(
                "errors[0]",  equalTo("You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM")
            );
    }

    @Test
    public void givenC100EnglishCase_whenPostRequestToGenerateDraftDoc_then200ResponseAndDocumentSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_GENERATE_DRAFT_DOC);

        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/generate-save-draft-document")
            .then()
            .assertThat().statusCode(200)
            .body("data.submitAndPayDownloadApplicationLink.document_filename", equalTo("Draft_C100_application.pdf"),
                "data.isEngDocGen", equalTo("Yes"));
    }

    @Test
    public void givenC100Case_whenPostRequestToIssueAndSend_then200ResponseAndFinalDocsSaved() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_ISSUE_AND_SEND);

        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/issue-and-send-to-local-court")
            .then()
            .assertThat().statusCode(200)
            .body("data.finalDocument.document_filename", equalTo("C100FinalDocument.pdf"),
                  "data.c1ADocument.document_filename", equalTo("C1A_Document.pdf"));
    }

    @Test
    public void givenC100Case_whenCaseUpdateEndpoint_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_UPDATE_APPLICATION);
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/update-application")
                            .content(replaceOldCaseInJsonWithNewGeneratedCaseId(requestBody))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth"))
            .andExpect(status().isOk())
            .andReturn();
    }



    @Test
    public void givenC100Case_whenCaseWithdrawnEndpoint_then200ResponseAndDataContainsUpdatedTabData() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_WITHDRAW_APPLICATION);
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/case-withdrawn-about-to-submit")
                            .content(replaceOldCaseInJsonWithNewGeneratedCaseId(requestBody))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.caseTypeOfApplication").value("C100"))
            .andExpect(jsonPath("data.withDrawApplicationData.withDrawApplication").value("Yes"))
            .andExpect(jsonPath("data.withDrawApplicationData.withDrawApplicationReason").value("test data"))
            .andExpect(jsonPath("data.isWithdrawRequestSent").value("Pending"))
            .andReturn();
    }

    @Test
    public void givenC100Case_whenSendToGateKeeperEndpoint_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPER);
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/send-to-gatekeeper")
                            .content(replaceOldCaseInJsonWithNewGeneratedCaseId(requestBody))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.gatekeepingDetails").isNotEmpty())
            .andReturn();
    }

    @Test
    public void givenC100Case_whenRpaResent_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/update-party-details")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"),
                  "data.caApplicant1Policy.OrgPolicyCaseAssignedRole",  equalTo("[C100APPLICANTSOLICITOR1]"),
                  "data.caRespondent1Policy.OrgPolicyCaseAssignedRole",  equalTo("[C100RESPONDENTSOLICITOR1]"));
    }

    @Test
    public void givenFl401Case_whenAboutToSubmitCaseCreation_then200ResponseAndApplicantNameUpdated() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_ABOUT_TO_SUBMIT_CREATION);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/update-party-details")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("FL401"),
                  "data.applicantName",  equalTo("test data"),
                  "data.applicantOrRespondentCaseName",  equalTo("thisIsATestName"),
                  "data.applicantsFL401.email",  equalTo("applicant@test.com"));
    }

    @Test
    public void givenC100CasePrePopulateCourtDetailsWithValidCourt() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/pre-populate-court-details")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"),
                  "data.courtList",  notNullValue());

    }

    @Test
    public void givenC100CasePrePopulateCourtDetailsWithoutValidCourt() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_RESEND_RPA);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/pre-populate-court-details")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"));


    }

    @Test
    public void testGatekeepingDetailsWhenLegalAdvisorOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPER);

        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/send-to-gatekeeper")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"),
                     "data.gatekeepingDetails",  notNullValue());
    }

    @Test
    public void testGatekeepingDetailsWhenJudgeOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPERJUDGE);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/send-to-gatekeeper")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"),
                  "data.isJudgeOrLegalAdviser",  equalTo("judge"),
                  "data.gatekeepingDetails",  notNullValue());
        // "data.gatekeepingDetails.isJudgeOrLegalAdviserGatekeeping",  equalTo("judge"))
    }

    @Test
    public void testAttachScanDocsWaChange() throws Exception {

        String requestBody = ResourceLoader.loadJson(C100_SEND_TO_GATEKEEPERJUDGE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/attach-scan-docs/about-to-submit")
            .then()
            .body("data.manageDocumentsRestrictedFlag", equalTo(TRUE),
                  "data.manageDocumentsTriggeredBy", equalTo(STAFF));

    }

    private String replaceOldCaseInJsonWithNewGeneratedCaseId(String requestBody) throws JsonProcessingException {
        CallbackRequest callbackRequest = objectMapper.readValue(requestBody, CallbackRequest.class);
        callbackRequest.getCaseDetails().setId(preCreatedCaseId);
        requestBody = objectMapper.writeValueAsString(callbackRequest);
        return requestBody;
    }

}
