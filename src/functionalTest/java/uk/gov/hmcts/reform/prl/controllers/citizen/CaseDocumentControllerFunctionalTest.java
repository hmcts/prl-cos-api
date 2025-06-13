package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(
    properties = {
        "idam.client.secret=${CITIZEN_IDAM_CLIENT_SECRET}",
        "idam.client.id=prl-citizen-frontend",
        "idam.s2s-auth.microservice=prl_citizen_frontend"
    }
)
@ContextConfiguration
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

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldSuccessfullyUploadDocument() throws Exception {
        //TODO Replace with citizen auth token once secrets added
        String filePath = "classpath:Test.pdf";
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document");

        response.then().assertThat().statusCode(200);
        DocumentResponse res = objectMapper.readValue(response.getBody().asString(), DocumentResponse.class);

        assertEquals("Success", res.getStatus());
        assertNotNull(res.getDocument());
        assertEquals("Test.pdf", res.getDocument().getDocumentFileName());
    }

    @Test
    public void shouldSuccessfullyDeleteDocument() throws Exception {
        String filePath = "classpath:Test.pdf";

        //TODO Replace with citizen auth token once secrets added
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document");

        DocumentResponse res = objectMapper.readValue(response.getBody().asString(), DocumentResponse.class);

        String[] documentSplit = res.getDocument().getDocumentUrl().split("/");

        Response deleteResponse = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .when()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .delete(String.format("/%s/delete", documentSplit[documentSplit.length - 1]));

        deleteResponse.then().assertThat().statusCode(200);
        DocumentResponse delRes = objectMapper.readValue(deleteResponse.getBody().asString(), DocumentResponse.class);

        assertEquals("Success", delRes.getStatus());
    }


    public static byte[] resourceAsBytes(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return Files.readAllBytes(file.toPath());
    }
}
