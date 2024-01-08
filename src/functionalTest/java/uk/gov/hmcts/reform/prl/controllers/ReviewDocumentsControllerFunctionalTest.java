package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ReviewDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    ObjectMapper objectMapper;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );


    private static final String REVIEW_DOCUMENT_REQUEST = "requests/review-doc-body.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private String requestBody;

    @Before
    public void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        requestBody = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST);
    }


    @Test
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidential() throws Exception {

        DocumentResponse docRes = uploadDocument();

        String requestBodyRevised = requestBody
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.restrictedDocuments[0].value.isConfidential", equalTo("No"),
                  "data.restrictedDocuments[0].value.isRestricted", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.applicantApplicationDocument.document_filename", equalTo("Confidential_Test.pdf"),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenOnlyConfidentialNotRestricted() throws Exception {

        DocumentResponse docRes = uploadDocument();

        String requestBodyRevised = requestBody
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.confidentialDocuments[0].value.isConfidential", equalTo("Yes"),
                  "data.confidentialDocuments[0].value.isRestricted", equalTo("No"),
                  "data.confidentialDocuments[0].value.applicantApplicationDocument.document_filename", equalTo("Confidential_Test.pdf"),
                  "data.restrictedDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYes() throws Exception {

        DocumentResponse docRes = uploadDocument();

        String requestBodyRevised = requestBody
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.restrictedDocuments[0].value.isConfidential", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.isRestricted", equalTo("Yes"),
                  "data.restrictedDocuments[0].value.applicantApplicationDocument.document_filename", equalTo("Confidential_Test.pdf"),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNo() throws Exception {

        DocumentResponse docRes = uploadDocument();

        String requestBodyRevised = requestBody
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.legalProfUploadDocListDocTab[0].value.isConfidential", equalTo("No"),
                  "data.legalProfUploadDocListDocTab[0].value.isRestricted", equalTo("No"),
                  "data.legalProfUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecNo() throws Exception {

        DocumentResponse docRes = uploadDocument();

        String requestBodyRevised = requestBody
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"")
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.legalProfUploadDocListDocTab[0].value.isConfidential", equalTo("Yes"),
                  "data.legalProfUploadDocListDocTab[0].value.isRestricted", equalTo("Yes"),
                  "data.legalProfUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    private DocumentResponse uploadDocument() throws JsonProcessingException {

        Response uploadResponse = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document");

        uploadResponse.then().assertThat().statusCode(200);
        return objectMapper.readValue(uploadResponse.getBody().asString(), DocumentResponse.class);
    }

}
