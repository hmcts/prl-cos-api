package uk.gov.hmcts.reform.prl.controllers.barrister;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "rd_professional.api.url=http://localhost:${wiremock.server.port}"
})
public class BarristerControllerFunctionalTest {
    public static final String APPLICATION_JSON = "application/json";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "controller/barrister-request-casedata-body.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @BeforeEach
    public void stubPrdEndpoints() {
        // Reset between tests to isolate behaviour
        WireMock.reset();

        // --- 1. /refdata/external/v1/organisations (findUserOrganisation)
        stubFor(get(urlPathEqualTo("/refdata/external/v1/organisations"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON)
                                    .withBody("""
                {
                  "organisationIdentifier": "ORG123",
                  "name": "Stubbed Org",
                  "contactInformation": []
                }
            """)));

        // --- 2. /refdata/internal/v1/organisations?id=...
        stubFor(get(urlPathEqualTo("/refdata/internal/v1/organisations"))
                    .withQueryParam("id", matching(".*"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON)
                                    .withBody("""
                {
                  "organisationIdentifier": "ORG123",
                  "name": "Stubbed Org",
                  "contactInformation": []
                }
            """)));

        // --- 3. /refdata/internal/v1/organisations?status=Active
        stubFor(get(urlPathEqualTo("/refdata/internal/v1/organisations"))
                    .withQueryParam("status", equalTo("Active"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON)
                                    .withBody("""
                [
                  { "organisationIdentifier": "ORG123", "name": "Stubbed Org", "contactInformation": [] }
                ]
            """)));

        // --- 4. /refdata/internal/v1/organisations/{orgId}/users (findOrganisationSolicitors)
        stubFor(get(urlMatching("/refdata/internal/v1/organisations/[^/]+/users"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON)
                                    .withBody("""
                {
                  "organisationIdentifier": "ORG123",
                  "orgSolicitors": []
                }
            """)));

        // --- 5. /refdata/external/v1/organisations/users/accountId (findUserByEmail)
        stubFor(get(urlPathEqualTo("/refdata/external/v1/organisations/users/accountId"))
                    .withHeader("UserEmail", matching(".+@.+"))
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", APPLICATION_JSON)
                                    .withBody("""
                { "userIdentifier": "11111111-2222-3333-4444-555555555555" }
            """)));
    }

    @Test
    public void testBarristerAddAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/barrister/add/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void testBarristerRemoveAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/barrister/remove/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void testBarristerStopRepresentingAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/barrister/stop-representing/about-to-start")
            .then()
            .assertThat().statusCode(500);
    }

    @Test
    public void testBarristerAddSubmittedCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/barrister/add/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void testBarristerRemoveSubmittedCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/barrister/remove/submitted")
            .then()
            .assertThat().statusCode(200);
    }

}
