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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;

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


    private static final String REVIEW_DOCUMENT_REQUEST_SOLICITOR = "requests/review-doc-body-solicitor.json";

    private static final String REVIEW_DOCUMENT_REQUEST_CAFCASS = "requests/review-doc-body-cafcass.json";

    private static final String REVIEW_DOCUMENT_REQUEST_COURT = "requests/review-doc-body-courtadmin.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private String requestBodyForSolitior;

    private String requestBodyForCafcass;

    private String requestBodyForCourtAdmin;


    @Before
    public void setUp() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        requestBodyForSolitior = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST_SOLICITOR);
        requestBodyForCafcass = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST_CAFCASS);
        requestBodyForCourtAdmin = ResourceLoader.loadJson(REVIEW_DOCUMENT_REQUEST_COURT);
    }


    @Test
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidentialForSol() throws Exception {

        DocumentResponse docRes = uploadDocument(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
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
    public void givenReviewDocuments_whenOnlyConfidentialNotRestrictedForSol() throws Exception {

        DocumentResponse docRes = uploadDocument(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
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
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesForSol() throws Exception {

        DocumentResponse docRes = uploadDocument(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
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
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoForSol() throws Exception {

        DocumentResponse docRes = uploadDocument(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
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
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecNoForSol() throws Exception {

        DocumentResponse docRes = uploadDocument(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
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

    private DocumentResponse uploadDocument(String uploadedBy) throws JsonProcessingException {

        String token = switch (uploadedBy) {
            case SOLICITOR -> idamTokenGenerator.generateIdamTokenForSolicitor();
            case CAFCASS -> idamTokenGenerator.generateIdamTokenForCafcass();
            case COURT_STAFF, COURT_ADMIN -> idamTokenGenerator.generateIdamTokenForCourtAdmin();
            default -> null;
        };

        Response uploadResponse = request
            .header("Authorization", token)
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .multiPart("file", new File("src/functionalTest/resources/Test.pdf"))
            .when()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/upload-citizen-document");

        uploadResponse.then().assertThat().statusCode(200);
        return objectMapper.readValue(uploadResponse.getBody().asString(), DocumentResponse.class);
    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecYesCafcass() throws Exception {

        DocumentResponse docRes = uploadDocument(CAFCASS);
        String requestBodyRevised = requestBodyForCafcass
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
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
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecNoCafcass() throws Exception {

        DocumentResponse docRes = uploadDocument(CAFCASS);
        String requestBodyRevised = requestBodyForCafcass
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"")
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.cafcassUploadDocListDocTab[0].value.isConfidential", equalTo("Yes"),
                  "data.cafcassUploadDocListDocTab[0].value.isRestricted", equalTo("Yes"),
                  "data.cafcassUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoCafcass() throws Exception {

        DocumentResponse docRes = uploadDocument(CAFCASS);
        String requestBodyRevised = requestBodyForCafcass
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.cafcassUploadDocListDocTab[0].value.isConfidential", equalTo("No"),
                  "data.cafcassUploadDocListDocTab[0].value.isRestricted", equalTo("No"),
                  "data.cafcassUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidentialForCafcass() throws Exception {

        DocumentResponse docRes = uploadDocument(CAFCASS);

        String requestBodyRevised = requestBodyForCafcass
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
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
    public void givenReviewDocuments_whenOnlyConfidentialNotRestrictedForCafcass() throws Exception {

        DocumentResponse docRes = uploadDocument(CAFCASS);

        String requestBodyRevised = requestBodyForSolitior
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
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
    public void givenReviewDocuments_whenOnlyConfidentialNotRestrictedForCourtAdmin() throws Exception {

        DocumentResponse docRes = uploadDocument(COURT_ADMIN);

        String requestBodyRevised = requestBodyForCourtAdmin
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
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
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidentialForCourtAdmin() throws Exception {

        DocumentResponse docRes = uploadDocument(COURT_ADMIN);

        String requestBodyRevised = requestBodyForCourtAdmin
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
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
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoCourtAdmin() throws Exception {

        log.info("givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoCourtAdmin.......");
        DocumentResponse docRes = uploadDocument(COURT_ADMIN);
        String requestBodyRevised = requestBodyForCourtAdmin
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"");

        AboutToStartOrSubmitCallbackResponse resp = request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            //.body("data.courtStaffUploadDocListDocTab[0].value.isConfidential", equalTo("No"),
            //      "data.courtStaffUploadDocListDocTab[0].value.isRestricted", equalTo("No"),
            //      "data.courtStaffUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
            //      "data.restrictedDocuments", equalTo(null),
            //      "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        log.info("RESULTTTTTT.......{}",resp);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecNoCourtAdmin() throws Exception {

        DocumentResponse docRes = uploadDocument(COURT_ADMIN);
        String requestBodyRevised = requestBodyForCourtAdmin
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"reviewDecisionYesOrNo\": \"yes\"",
                     "\"reviewDecisionYesOrNo\": \"no\"")
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/review-documents/about-to-submit")
            .then()
            .body("data.courtStaffUploadDocListDocTab[0].value.isConfidential", equalTo("Yes"),
                  "data.courtStaffUploadDocListDocTab[0].value.isRestricted", equalTo("Yes"),
                  "data.courtStaffUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecYesCourtAdmin() throws Exception {

        DocumentResponse docRes = uploadDocument(COURT_ADMIN);
        String requestBodyRevised = requestBodyForCourtAdmin
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isConfidential\": \"No\"",
                     "\"isConfidential\": \"Yes\"")
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        request1
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
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


}
