package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseFlagsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static CaseDetails caseDetails;

    private static final String CASE_FLAG_CONTROLLER_REQUEST_JSON = "requests/case-flag-controller-request.json";
    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());
    }

    @Test
    public void givenBodyWithCaseFlags_whenAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        String requestBodyRevised = requestBody
            .replace("1701870369166430", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/caseflags/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenBodyWithCaseFlags_whenAboutToSubmit() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        String requestBodyRevised = requestBody
            .replace("1701870369166430", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/caseflags/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBodyWithCaseFlags_whenSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        String requestBodyRevised = requestBody
            .replace("1701870369166430", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/caseflags/submitted")
            .then()
            .assertThat().statusCode(200);

    }

}
