package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseDocumentControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String GENERATE_UPLOAD_DOCUMENT_REQUEST = "requests/generate-document-request.json";
    public static final String DUMMY_UPLOAD_FILE = "classpath:requests/Dummy_pdf_file.pdf";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldSuccessfullyUploadDocument() throws Exception {
        //TODO Replace with citizen auth token once secrets added
        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", fileToUpload)
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-statement-document");

        response.then().assertThat().statusCode(200);
        DocumentResponse res = objectMapper.readValue(response.getBody().asString(), DocumentResponse.class);

        Assert.assertEquals("Success", res.getStatus());
        Assert.assertNotNull(res.getDocument());
        Assert.assertEquals("Dummy_pdf_file.pdf", res.getDocument().getDocumentFileName());
    }

    @Test
    public void shouldSuccessfullyDeleteDocument() throws Exception {
        //TODO Replace with citizen auth token once secrets added
        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", fileToUpload)
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-statement-document");

        DocumentResponse res = objectMapper.readValue(response.getBody().asString(), DocumentResponse.class);

        String[] documentSplit = res.getDocument().getDocumentUrl().split("/");

        Response deleteResponse = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .delete(String.format("/%s/delete", documentSplit[documentSplit.length - 1]));

        deleteResponse.then().assertThat().statusCode(200);
        DocumentResponse delRes = objectMapper.readValue(deleteResponse.getBody().asString(), DocumentResponse.class);

        Assert.assertEquals("Success", delRes.getStatus());
    }

    @Test
    public void givenGenerateDocumentForCitizen_return200() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenGenerateDocumentForCitizen_return400() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization", "test s2sToken")
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then()
            .assertThat().statusCode(400);

    }

    @Test
    public void givenGenerateDocumentForCitizen_return404() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", "auth")
            .header(
                "serviceAuthorization", "test s2sToken")
            .when()
            .contentType("application/json")
            .post("/generate-citizen-document")
            .then()
            .assertThat().statusCode(405);

    }

    @Test
    public void givenDeleteDocumentForCitizen_return200() throws Exception {
        String requestBody = ResourceLoader.loadJson(GENERATE_UPLOAD_DOCUMENT_REQUEST);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-citizen-statement-document")
            .then().assertThat().statusCode(200);

    }

    @Test
    public void givenUploadDocumentForCitizen_return200() throws Exception {
        final File fileToUpload = ResourceLoader.readFile(DUMMY_UPLOAD_FILE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file",fileToUpload)
            .param("typeOfDocument", "C8")
            .pathParam("caseId","1667826894103746")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document")
            .then().assertThat().statusCode(200);

    }

}
