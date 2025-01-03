package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TRUE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
@SuppressWarnings("unchecked")
public class CallbackControllerFT {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;


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

    /*@Ignore
    @Test
    public void givenC100Case_whenCaseUpdateEndpoint_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_UPDATE_APPLICATION);
        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/update-application")
            .then()
            .assertThat().statusCode(200);
    }*/

    @Test
    public void givenC100Case_whenCaseWithdrawnEndpoint_then200ResponseAndDataContainsUpdatedTabData() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_WITHDRAW_APPLICATION);

        request
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/case-withdrawn-about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.welshLanguageRequirementsTable", notNullValue(),
                "data.otherProceedingsDetailsTable", notNullValue(),
                  "data.allegationsOfHarmDomesticAbuseTable", notNullValue(),
                "data.summaryTabForOrderAppliedFor", notNullValue(),
                "data.miamTable", notNullValue()
            );

    }

    @Test
    public void givenC100Case_whenSendToGateKeeperEndpoint_then200Response() throws Exception {
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
            .body("data.gatekeepingDetails", notNullValue());

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


}
