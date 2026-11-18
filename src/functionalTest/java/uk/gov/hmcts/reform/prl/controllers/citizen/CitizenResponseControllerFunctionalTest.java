package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
class CitizenResponseControllerFunctionalTest {

    private static final String GENERATE_C7_ENDPOINT = "/citizen/{caseId}/{partyId}/generate-c7document";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance = StringUtils.defaultIfBlank(System.getenv("TEST_URL"), "http://localhost:4044");

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    void givenNoAuthHeaders_whenGenerateC7Document_thenBadRequest() {
        request
            .pathParam("caseId", "1234567890123456")
            .pathParam("partyId", "00000000-0000-0000-0000-000000000000")
            .body("{\"welsh\":false}")
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post(GENERATE_C7_ENDPOINT)
            .then()
            .assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void givenInvalidServiceToken_whenGenerateC7Document_thenServerError() {
        request
            .pathParam("caseId", "1234567890123456")
            .pathParam("partyId", "00000000-0000-0000-0000-000000000000")
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, "Bearer invalid-s2s-token")
            .body("{\"welsh\":false}")
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post(GENERATE_C7_ENDPOINT)
            .then()
            .assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}

