package uk.gov.hmcts.reform.prl.controllers.cafcass;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.LinkedHashMap;

import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CafcassDocumentManagementControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY = "requests/c100-respondent-solicitor-call-back-controller.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);



    @Autowired
    CaseDocumentClient caseDocumentClient;

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private SystemUserService systemUserService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static String documentId;

    @Test
    @Order(1)
    public void createDocumentsForTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/generate-c7response-document")
            .then()
            .body("data.draftC7ResponseDoc.document_filename", equalTo("Draft_C7_response.pdf"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        LinkedHashMap draftC7ResponseDoc = (LinkedHashMap) responseData.getData().get("draftC7ResponseDoc");
        documentId = StringUtils.substringAfter(draftC7ResponseDoc.get("document_url").toString(), "/documents/");

    }

    @Test
    @Order(2)
    public void givenValidUuidDownloadFileWith200Response() throws Exception {

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType("application/json")
            .get("/cases/documents/{documentId}/binary", documentId)
            .then()
            .assertThat().statusCode(200)
            .contentType("application/pdf")
            .header("content-type", equalTo("application/pdf"))
            .header("originalfilename",equalTo("C7_Response_Draft_Document.pdf"))
            .extract()
            .response();

    }

    @Test
    public void givenInvalidUuidDownloadFileWith400Response() throws Exception {
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType("application/json")
            .get("/cases/documents/{documentId}/binary", "incorrectDocId")
            .then()
            .assertThat().statusCode(400)
            .extract()
            .response();


    }
}
