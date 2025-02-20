package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.List;

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
        caseDetails = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-about-to-start")
            .then()
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        List<Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient = (List<Element<StmtOfServiceAddRecipient>>) caseDetails.getData().get(
            "stmtOfServiceAddRecipient");
        Assert.assertEquals(1, stmtOfServiceAddRecipient.size());
    }

    @Test
    public void testStmtOfServiceAboutToSubmit() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        CaseData caseData = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-about-to-submit")
            .then()
            .extract()
            .as(CaseData.class);

        Assert.assertNotNull(caseData);
        Assert.assertNull(caseData.getStatementOfService().getStmtOfServiceWhatWasServed());
    }

    @Test
    public void testStmtOfServiceSubmitted() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        SubmittedCallbackResponse response = request
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem())
            .header(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/Statement-of-service-confirmation")
            .then()
            .extract()
            .as(SubmittedCallbackResponse.class);
        Assert.assertEquals("# Application was served", response.getConfirmationHeader());
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
            .pathParam("caseId", "1711105989241323")
            .pathParam("eventId","citizenStatementOfService")
            .post("/{caseId}/{eventId}/save-statement-of-service-by-citizen")
            .then()
            .statusCode(200);

    }
}
