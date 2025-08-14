package uk.gov.hmcts.reform.prl.controllers.cafcass;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_DUMMY_UPLOAD_FILE;
import static uk.gov.hmcts.reform.prl.utils.TestResourceUtil.readFile;

/**
 * functional test case for cafcass safegaurding letter upload.
 *<p></p>
 *  ignored the test case as caseId won't be available in PR & higher environment.
 */
@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CafcassUploadDocumentFunctionalTest {

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
    @Order(1)
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
    @Order(2)
    public void givenValidDocumentData_then200Response() throws IOException {
        final File fileToUpload = readFile(CAFCASS_DUMMY_UPLOAD_FILE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .multiPart("file",fileToUpload)
            .param("typeOfDocument", "Letter_from_Child")
            .pathParam("caseId",caseDetails.getId())
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/{caseId}/document")
            .then().assertThat().statusCode(200)
            .body("message", equalTo("Document has been uploaded successfully: Dummy_pdf_file.pdf"));

    }
}
