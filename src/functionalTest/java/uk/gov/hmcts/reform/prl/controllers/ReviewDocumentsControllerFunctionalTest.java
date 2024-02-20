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
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
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
    protected CaseDocumentClient caseDocumentClient;

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


    @Test//f1
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidentialForSol() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(SOLICITOR);

        String requestBodyRevised = requestBodyForSolitior
            .replace("http://dm-store-aat.service.core-compute-aat.internal/documents/docId",
                     docRes.getDocument().getDocumentUrl())
            .replace("\"isRestricted\": \"No\"",
                     "\"isRestricted\": \"Yes\"");

        AboutToStartOrSubmitCallbackResponse resp = request1
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
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenReviewDocuments_whenOnlyConfidentialNotRestrictedForSol() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(SOLICITOR);

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

    public static byte[] resourceAsBytes(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return Files.readAllBytes(file.toPath());
    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesForSol() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(SOLICITOR);

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

    private DocumentResponse uploadDocumentIntoCdam(String uploadedBy) throws IOException {

        String token = switch (uploadedBy) {
            case SOLICITOR -> idamTokenGenerator.generateIdamTokenForSolicitor();
            case CAFCASS -> idamTokenGenerator.generateIdamTokenForCafcass();
            case COURT_STAFF, COURT_ADMIN -> idamTokenGenerator.generateIdamTokenForCourtAdmin();
            default -> null;
        };

        String filePath = "classpath:Test.pdf";
        final MultipartFile file = new InMemoryMultipartFile(filePath, filePath, MediaType.APPLICATION_PDF_VALUE,
                                                             resourceAsBytes(filePath));

        UploadResponse response = caseDocumentClient.uploadDocuments(token, serviceAuthenticationGenerator.generate(),
                                                                     CASE_TYPE, JURISDICTION, newArrayList(file)
        );

        uk.gov.hmcts.reform.ccd.document.am.model.Document stampedDocument = response.getDocuments().get(0);

        return DocumentResponse.builder().status("Success").document(Document.builder()
                                                                                         .documentBinaryUrl(stampedDocument.links.binary.href)
                                                                                         .documentUrl(stampedDocument.links.self.href)
                                                                                         .documentFileName(stampedDocument.originalDocumentName)
                                                                                         .documentCreatedOn(stampedDocument.createdOn)
                                                                                         .build()).build();



    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoForSol() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(SOLICITOR);

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
            .body("data.legalProfUploadDocListDocTab[0].value.isConfidential", equalTo(null),
                  "data.legalProfUploadDocListDocTab[0].value.isRestricted", equalTo(null),
                  "data.legalProfUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);


    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedYesAndReviewDecNoForSol() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(SOLICITOR);

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
            .body("data.legalProfUploadDocListDocTab[0].value.isConfidential", equalTo(null),
                  "data.legalProfUploadDocListDocTab[0].value.isRestricted", equalTo(null),
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

        DocumentResponse docRes = uploadDocumentIntoCdam(CAFCASS);
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

        DocumentResponse docRes = uploadDocumentIntoCdam(CAFCASS);
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
            .body("data.cafcassUploadDocListDocTab[0].value.isConfidential", equalTo(null),
                  "data.cafcassUploadDocListDocTab[0].value.isRestricted", equalTo(null),
                  "data.cafcassUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test.pdf"),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenBothConfidentialAndRestrictedNoAndReviewDecNoCafcass() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(CAFCASS);
        String requestBodyRevised = requestBodyForCafcass
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
            .body("data.cafcassUploadDocListDocTab[0].value.isConfidential", equalTo(null),
                  "data.cafcassUploadDocListDocTab[0].value.isRestricted", equalTo(null),
                  "data.cafcassUploadDocListDocTab[0].value.cafcassQuarantineDocument.document_filename", equalTo(null),
                  "data.restrictedDocuments", equalTo(null),
                  "data.confidentialDocuments", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenReviewDocuments_whenOnlyRestrictedNotConfidentialForCafcass() throws Exception {

        DocumentResponse docRes = uploadDocumentIntoCdam(CAFCASS);

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

        DocumentResponse docRes = uploadDocumentIntoCdam(CAFCASS);

        String requestBodyRevised = requestBodyForCafcass
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

}
