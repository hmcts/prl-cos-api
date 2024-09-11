package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StatementOfServiceControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
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

    private static CaseDetails caseDetails;

    @Test
    public void testStmtOfServiceAboutToStart() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-about-to-start")
            .then()
            .body("stmtOfServiceAddRecipient[0].value.respondentDynamicList", Matchers.hasLength(2))
            .extract()
            .as(CaseData.class);
    }

    @Test
    public void testStmtOfServiceAboutToSubmit() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-about-to-submit")
            .then()
            .body("stmtOfServiceForApplication", equalTo("citizen@email.com"),
                  "stmtOfServiceForOrder", equalTo("07442772347"),
                  "stmtOfServiceAddRecipient", equalTo(null),
                  "stmtOfServiceWhatWasServed", equalTo(null))
            .extract()
            .as(CaseData.class);
    }

    @Test
    public void testStmtOfServiceSubmitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-confirmation")
            .then()
            .body("confirmationHeader", equalTo("# Application was served"))
            .extract()
            .as(CaseData.class);
    }

    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .pathParam("caseId", String.valueOf(caseDetails.getId()))
            .pathParam("eventId","citizenStatementOfService")
            .post("/{caseId}/{eventId}/save-statement-of-service-by-citizen")
            .then()
            .body("finalServedApplicationDetailsList", equalTo("citizen@email.com"),
                  "unServedRespondentPack", equalTo(null))
            .extract()
            .as(CaseData.class);
    }
}
