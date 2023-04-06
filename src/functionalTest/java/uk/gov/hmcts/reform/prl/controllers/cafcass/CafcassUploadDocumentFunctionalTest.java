package uk.gov.hmcts.reform.prl.controllers.cafcass;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.io.IOException;

import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_DUMMY_UPLOAD_FILE;
import static uk.gov.hmcts.reform.prl.utils.TestResourceUtil.readFile;

/**
 * functional test case for cafcass safegaurding letter upload.
 *<p></p>
 *  ignored the test case as caseId won't be available in PR & higher environment.
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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


    @Test
    public void givenValidDocumentData_then200Response() throws IOException {
        final File fileToUpload = readFile(CAFCASS_DUMMY_UPLOAD_FILE);

        request
            .header(
                "Authorization",
                idamTokenGenerator.generateIdamTokenForCafcass())
            .header(
                "ServiceAuthorization",
                serviceAuthenticationGenerator.generateApiGwServiceAuth()
            )
            .multiPart("file",fileToUpload)
            .param("typeOfDocument", "C8")
            .pathParam("caseId","1667241261380614")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/{caseId}/document")
            .then().assertThat().statusCode(200);


    }
}
