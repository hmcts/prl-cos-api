package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
@TestPropertySource(
    properties = {
        "idam.client.secret=${CITIZEN_IDAM_CLIENT_SECRET}",
        "idam.client.id=prl-citizen-frontend",
        "idam.s2s-auth.microservice=prl_citizen_frontend"
    }
)
@SuppressWarnings("PMD")
public class CaseApplicationResponseControllerFunctionalTest {

    private static final String GENERATE_C7_ENDPOINT = "/citizen/{caseId}/{partyId}/generate-c7document";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request =
        RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void givenNoAuthHeaders_whenGenerateC7Document_thenUnauthorised() {
        Response response = request
            .pathParam("caseId", "1234567890123456")
            .pathParam("partyId", "00000000-0000-0000-0000-000000000000")
            .body("{\"welsh\":false}")
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post(GENERATE_C7_ENDPOINT);

        // Endpoint rejects unauthenticated requests before any case processing.
        response.then().assertThat().statusCode(anyOf(is(401), is(403), is(400)));
    }

    @Test
    public void givenInvalidServiceToken_whenGenerateC7Document_thenRejected() {
        Response response = request
            .pathParam("caseId", "1234567890123456")
            .pathParam("partyId", "00000000-0000-0000-0000-000000000000")
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("serviceAuthorization", "Bearer invalid-s2s-token")
            .body("{\"welsh\":false}")
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post(GENERATE_C7_ENDPOINT);

        // Invalid S2S token must not be authorised.
        response.then().assertThat().statusCode(anyOf(is(401), is(403), is(500)));
    }
}

