package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
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
public class ServiceOfApplicationControllerFTest {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String APPLICATION_JSON = "application/json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestWithCaseDataOkResponseForAboutToStart() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/service-of-application/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestWithCaseDataResponseContainsHeaderAndCollapsable() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        RestAssured.given().spec(request)
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/service-of-application/about-to-start")
            .then().assertThat()
            .statusCode(200)
            .body("isConfidential", Matchers.equalTo("No"))
            .body("soaDocumentDynamicListForLa", Matchers.anything())
            .body("sentDocumentPlaceHolder", Matchers.anything());
    }

    @Test
    public void givenRequestWithCaseDataResponseAboutToSubmit() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        RestAssured.given().spec(request)
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/service-of-application/about-to-submit")
            .then().assertThat()
            .statusCode(200)
            .body("caseManagementLocation", Matchers.anything())
            .body("proceedToServing", Matchers.equalTo("Yes"));
    }

    @Test
    public void givenRequestWithCaseDataResponseSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        RestAssured.given().spec(request)
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON)
            .post("/service-of-application/submitted")
            .then().assertThat()
            .statusCode(200)
            .body("finalServedApplicationDetailsList", Matchers.anything());
    }
}
