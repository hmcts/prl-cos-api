package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

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
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;


@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class C100RespondentSolicitorControllerFunctionalTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final String userToken = "Bearer testToken";
    private static final String VALID_REQUEST_BODY = "requests/c100-respondent-solicitor-call-back-controller.json";

    private static final String VALID_REQUEST_BODY_FOR_C1A_DRAFT = "requests/c100-respondent-solicitor-c1adraft-generate.json";

    private static final String VALID_REQUEST_BODY_FOR_C1A_FINAL = "requests/c100-respondent-solicitor-c1afinal-generate.json";

    private static final String VALID_REQUEST_BODY_C1A = "requests/c100-respondent-solicitor-C1A.json";

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

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
    public void givenRequestBody_whenRespondent_solicitor_about_to_start_then200Response() throws Exception {
        log.info("viola");
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/about-to-start")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("solicitorName"));
        Assert.assertEquals("AAT Solicitor",responseData.getData().get("solicitorName"));
    }

    @Test
    @Order(3)
    public void givenRequestBody_whenRespondent_solicitor_about_to_submit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/about-to-submit")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("solicitorName"));
        Assert.assertEquals("AAT Solicitor",responseData.getData().get("solicitorName"));
    }

    @Test
    @Order(4)
    public void givenRequestBody_whenAbout_to_start_response_validation_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/about-to-start-response-validation")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("solicitorName"));
        Assert.assertEquals("AAT Solicitor",responseData.getData().get("solicitorName"));
    }

    @Test
    @Order(5)
    public void givenRequestBody_whenSubmit_c7_response_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/submit-c7-response")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("solicitorName"));
        Assert.assertEquals("AAT Solicitor",responseData.getData().get("solicitorName"));
    }

    @Test
    @Order(6)
    public void givenRequestBody_whenKeep_details_private_list_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/keep-details-private-list")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("confidentialListDetails"));
        Assert.assertEquals("<ul><li>Telephone number</li></ul>",responseData.getData().get("confidentialListDetails"));
    }

    @Test
    @Order(7)
    public void givenRequestBody_whenGenerate_c7response_draft_document_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
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
    }

    @Test
    @Order(8)
    public void givenRequestBody_whenAbout_to_start_response_validation_then200Response_C1A() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_C1A);

        AboutToStartOrSubmitCallbackResponse responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/about-to-start-response-validation")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getData().get("solicitorName"));
        Assert.assertEquals("AAT Solicitor",responseData.getData().get("solicitorName"));

    }

    /**
     * Respondent solicitor ViewResponseDraftDocument journey - C1A both English and Welsh Draft document should be generated.
     */
    @Test
    @Order(9)
    public void givenRequestBody_whenGenerate_c7c1a_draft_welshAndEnglish_document() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_FOR_C1A_DRAFT);
        String requestBodyRevised = requestBody
            .replace("1709890737146296", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/generate-c7response-document")
            .then()
            .body("data.draftC1ADocWelsh.document_filename", equalTo("Draft_C1A_allegation_of_harm_Welsh.pdf"),
                  "data.draftC1ADoc.document_filename", equalTo("Draft_C1A_allegation_of_harm.pdf"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    /**
     * Respondent solicitor submit journey - C1A both English and Welsh Final document should be generated
     * and moved to quarantine (Documents To be Reviewed).
     */
    @Test
    @Order(10)
    public void givenRequestBody_whenSubmit_c7C1A_final_welshAndEnglish_document() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_FOR_C1A_FINAL);

        String requestBodyRevised = requestBody
            .replace("1709204361187289", caseDetails.getId().toString());

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/submit-c7-response")
            .then()
            .body("data.legalProfQuarantineDocsList[2].value.document.document_filename", equalTo("C1A_allegation_of_harm.pdf"),
                  "data.legalProfQuarantineDocsList[2].value.categoryId", equalTo("respondentC1AApplication"),
                  "data.legalProfQuarantineDocsList[2].value.categoryName", equalTo("Respondent C1A Application"),
                  "data.legalProfQuarantineDocsList[3].value.document.document_filename", equalTo("Final_C1A_allegation_of_harm_Welsh.pdf"),
                  "data.legalProfQuarantineDocsList[3].value.categoryId", equalTo("respondentC1AApplication"),
                  "data.legalProfQuarantineDocsList[3].value.categoryName", equalTo("Respondent C1A Application"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

}
