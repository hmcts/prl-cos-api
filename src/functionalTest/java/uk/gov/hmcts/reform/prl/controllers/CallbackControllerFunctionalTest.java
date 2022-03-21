package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CallbackControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
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

    @Test
    public void givenNoMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndMiamError() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-miam-application-or-exemption")
            .then()
            .body("errors",
                  contains(
                      "You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM")
            )
            .assertThat().statusCode(200);
    }

    @Test
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_NO_ERROR);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-miam-application-or-exemption")
            .then()
            .body("errors", Matchers.hasSize(0))
            .assertThat().statusCode(200);
    }


    @Test
    public void givenNoAuthorization_whenPostRequestToDraftDocumentGeneration_then400Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-save-draft-document")
            .then().assertThat().statusCode(400);
    }


    @Test
    public void givenRequestWithApplicantOrRespondentCaseName_whenEndPointCalled_ResponseContainsApplicantCaseName() throws Exception {
        String requestBody = ResourceLoader.loadJson(APPLICANT_CASE_NAME_REQUEST);
        request
            .header("Authorization", userToken)
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
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-application-consideration-timetable")
            .then()
            .body("errors", contains("Please provide either days or hours in proposed timetable"))
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestWithC100ApplicantDetails_whenEndPointCalled_ResponseContainsTypeOfApplication() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_APPLICANT_DETAILS);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-withdrawn-email-notification")
            .then()
            .body("data.caseTypeOfApplication", equalTo("C100"))
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestWithFL401ApplicantDetails_whenEndPointCalled_ResponseContainsTypeOfApplication() throws Exception {
        String requestBody = ResourceLoader.loadJson(C100_APPLICANT_DETAILS);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-withdrawn-email-notification")
            .then()
            .body("data.caseTypeOfApplication", equalTo("FL401"))
            .assertThat().statusCode(200);
    }

}
