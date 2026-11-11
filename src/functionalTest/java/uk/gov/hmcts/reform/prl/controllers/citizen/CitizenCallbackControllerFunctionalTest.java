package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
class CitizenCallbackControllerFunctionalTest {

    private static final String CREATE_CCD_CASE_ENDPOINT = "/testing-support/create-ccd-case-data";
    private static final String UPDATE_CITIZEN_APPLICATION_ENDPOINT = "/update-citizen-application";
    private static final String CITIZEN_CASE_CREATION_SUBMITTED_ENDPOINT = "/citizen-case-creation-callback/submitted";
    private static final String CITIZEN_CASE_WITHDRAWN_ENDPOINT = "/citizen-case-withdrawn-notification";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Test
    void givenRequestBodyWhenUpdateCitizenApplicationThen200Response() throws Exception {
        CaseDetails caseDetails = createCase();

        invokeCallback(UPDATE_CITIZEN_APPLICATION_ENDPOINT, caseDetails)
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    void givenRequestBodywhenCitizenCaseCreationCallbackSubmittedthen200Response() throws Exception {
        CaseDetails caseDetails = createCase();

        invokeCallback(CITIZEN_CASE_CREATION_SUBMITTED_ENDPOINT, caseDetails)
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    void givenRequestBodyWhenSendNotificationsOnCaseWithdrawnThen200Response() throws Exception {
        CaseDetails caseDetails = createCase();

        invokeCallback(CITIZEN_CASE_WITHDRAWN_ENDPOINT, caseDetails)
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    void givenNoAuthHeadersWhenUpdateCitizenApplicationThenBadRequest() {
        RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(CallbackRequest.builder().build())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(UPDATE_CITIZEN_APPLICATION_ENDPOINT)
            .then()
            .assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenNoAuthHeadersWhenSendNotificationsOnCaseWithdrawnThenBadRequest() {
        RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(CallbackRequest.builder().build())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CITIZEN_CASE_WITHDRAWN_ENDPOINT)
            .then()
            .assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private CaseDetails createCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        return request
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(CREATE_CCD_CASE_ENDPOINT)
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);
    }

    private Response invokeCallback(String endpoint, CaseDetails caseDetails) {
        return RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build())
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post(endpoint);
    }
}
