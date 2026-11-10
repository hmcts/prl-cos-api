package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class TestingSupportControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;
    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance = StringUtils.defaultIfBlank(System.getenv("TEST_URL"), "http://localhost:4044");
    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    void testCreateDummyCitizenCase() {
        CaseData caseData = request
            .header(AUTHORIZATION_HEADER, idamTokenGenerator.generateIdamTokenForCitizen())
            .header(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .post("/testing-support/create-dummy-citizen-case")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(CaseData.class);
        Assertions.assertTrue(caseData.getId() > 0);
    }
}
