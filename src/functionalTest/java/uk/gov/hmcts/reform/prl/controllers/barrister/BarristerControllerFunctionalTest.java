package uk.gov.hmcts.reform.prl.controllers.barrister;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
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

    private final RequestSpecification request = RestAssured.given()
        .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
        .relaxedHTTPSValidation()
        .baseUri(targetInstance);

    @Test
    public void testBarristerAddAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        Response resp = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .contentType("application/json")
            .body(requestBody)
            .post("/barrister/add/about-to-start");

        // Print everything when not 200 so Jenkins shows the message/cause
        if (resp.statusCode() != 200) {
            System.out.println("=== RESPONSE " + resp.statusLine() + " ===");
            System.out.println(resp.asString());
        }

        resp.then().statusCode(200);
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
