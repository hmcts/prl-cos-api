package uk.gov.hmcts.reform.prl.controllers.managedocuments;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.DETAILS_ERROR_MESSAGE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManageDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String MANAGE_DOCUMENT_REQUEST = "requests/manage-documents-request.json";

    private static final String APPLICANT_APPLICATION_NAME = "Applicant application";


    private static final String MANAGE_DOCUMENT_COURT_REQUEST =
        "requests/manage-documents-request-with-court-as-party.json";
    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED = "requests/manage-documents-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED = "requests/manage-documents-neitherConfNorRestricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN = "requests/manage-documents-restricted-admin.json";

    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static CaseDetails caseDetails;

    @Test
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
    public void givenCaseId_whenAboutToStartEndPoint_thenRespWithDocumentCategories() throws Exception {

        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/about-to-start")
            .then()
            .body("data.manageDocuments[0].value.documentParty", equalTo(null),
                  "data.manageDocuments[0].value.document", equalTo(null),
                  "data.manageDocuments[0].value.documentDetails", equalTo(null),
                  "data.manageDocuments[0].value.documentRestrictCheckbox", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.code", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.label",equalTo(null)
            )
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsEndPoint_thenRespWithCopiedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Ignore
    @Test
    public void givenManageDocuments_GiveErrorWhenCourtAdminUserSelectCourt() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_COURT_REQUEST);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/mid-event")
            .then()
            .body("errors",
                  contains("Only court admin/Judge can select the value 'court' for 'submitting on behalf of'"))
            .assertThat().statusCode(200);
    }

    @Ignore
    @Test
    public void givenManageDocuments_ShouldNotGiveErrorWhenCourtAdminUserSelectCourt() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_COURT_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/mid-event")
            .then()
            .body("errors", equalTo(null))
            .assertThat().statusCode(200);
    }

    // ignoring this as managedocument event is working in demo probabaly we need to update the json here
    public void givenCaseId_whenCopy_manage_docsEndPoint_thenRespWithCopiedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Ignore
    @Test
    public void givenManageDocuments_whenCopy_manage_docsMid_thenCheckDocumentField_WhenNotRestricted() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/mid-event")
            .then()
            .body("errors", equalTo(null))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsMid_thenCheckDocumentField_WhenRestricted() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/mid-event")
            .then()
            .body("errors[0]", equalTo(DETAILS_ERROR_MESSAGE))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenMangeDocs_whenCopyDocsNeitherConfNorRestricted_thenAppropriateCategoryForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCourtAdmin() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenMangeDocs_whenCopyDocsNeitherConfNorRestricted_thenAppropriateCategoryForCourtAdmin() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN);

        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString())
            .replace("\"isConfidential\": \"Yes\"",
                     "\"isConfidential\": \"No\"")
            .replace("\"isRestricted\": \"Yes\"",
                     "\"isRestricted\": \"No\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);

    }

}
