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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource("classpath:application.yaml")
@SuppressWarnings("PMD")
public class CaseDocumentControllerFunctionalTest {

    @Autowired
    ObjectMapper objectMapper;

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

    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldSuccessfullyUploadDocument() throws Exception {
        Response response = request
                .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
                .when()
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .post("/upload-citizen-statement-document");

        response.then().assertThat().statusCode(200);
        DocumentResponse res = objectMapper.readValue(response.getBody().asString(), DocumentResponse.class);

        Assert.assertEquals("Success", res.getStatus());
        Assert.assertNotNull(res.getDocument());
        Assert.assertEquals("Test.pdf", res.getDocument().getDocumentFileName());
    }

    @Test
    public void shouldSuccessfullyDeleteDocument() throws Exception {
        Response response = request
                .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
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
}