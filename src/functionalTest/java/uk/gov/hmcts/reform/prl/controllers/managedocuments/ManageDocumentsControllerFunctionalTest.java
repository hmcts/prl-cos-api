package uk.gov.hmcts.reform.prl.controllers.managedocuments;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.COURT_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.HOME_OFFICE_DWP_RESPONSES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LETTERS_OF_COMPLAINTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICES_OF_ACTING_DISCHARGE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PUBLIC_FUNDING_CERTIFICATES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SAFEGUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION7_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SIXTEEN_A_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_GUARDIANSHIP_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPIP_REFERRAL_REQUESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.WITNESS_AVAILABILITY;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.DETAILS_ERROR_MESSAGE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageDocumentsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String MANAGE_DOCUMENT_REQUEST = "requests/manage-documents-request.json";

    private static final String APPLICANT_APPLICATION_NAME = "Applicant application";


    private static final String MANAGE_DOCUMENT_COURT_REQUEST =
        "requests/manage-documents-request-with-court-as-party.json";
    //private static final String MANAGE_DOCUMENT_RESPONSE = "response/MangeDocument.json";

    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED = "requests/manage-documents-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED = "requests/manage-documents-neitherConfNorRestricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN = "requests/manage-documents-restricted-admin.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenCaseId_whenAboutToStartEndPoint_thenRespWithDocumentCategories() throws Exception {

        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/about-to-start")
            .then()
            .body("data.manageDocuments[0].value.documentParty", equalTo(null),
                  "data.manageDocuments[0].value.document", equalTo(null),
                  "data.manageDocuments[0].value.documentDetails", equalTo(null),
                  "data.manageDocuments[0].value.documentRestrictCheckbox", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.code", equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.value.label",equalTo(null),
                  "data.manageDocuments[0].value.documentCategories.list_items[0].code", equalTo(APPLICANT_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[1].code", equalTo(APPLICANT_C1A_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[2].code", equalTo(APPLICANT_C1A_RESPONSE),
                  "data.manageDocuments[0].value.documentCategories.list_items[3].code", equalTo(MIAM_CERTIFICATE),
                  "data.manageDocuments[0].value.documentCategories.list_items[4].code", equalTo(PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[5].code", equalTo(RESPONDENT_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[6].code", equalTo(RESPONDENT_C1A_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[7].code", equalTo(RESPONDENT_C1A_RESPONSE),
                  "data.manageDocuments[0].value.documentCategories.list_items[8].code", equalTo(ORDERS_FROM_OTHER_PROCEEDINGS),
                  "data.manageDocuments[0].value.documentCategories.list_items[9].code", equalTo(NOTICE_OF_HEARING),
                  "data.manageDocuments[0].value.documentCategories.list_items[10].code", equalTo(COURT_BUNDLE),
                  "data.manageDocuments[0].value.documentCategories.list_items[11].code", equalTo(CASE_SUMMARY),
                  "data.manageDocuments[0].value.documentCategories.list_items[12].code", equalTo(SAFEGUARDING_LETTER),
                  "data.manageDocuments[0].value.documentCategories.list_items[13].code", equalTo(SECTION7_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[14].code", equalTo(SECTION_37_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[15].code", equalTo(SIXTEEN_A_RISK_ASSESSMENT),
                  "data.manageDocuments[0].value.documentCategories.list_items[16].code", equalTo(GUARDIAN_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[17].code", equalTo(SPECIAL_GUARDIANSHIP_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[18].code", equalTo(OTHER_DOCS),
                  "data.manageDocuments[0].value.documentCategories.list_items[19].code", equalTo(EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED),
                  "data.manageDocuments[0].value.documentCategories.list_items[20].code", equalTo(PUBLIC_FUNDING_CERTIFICATES),
                  "data.manageDocuments[0].value.documentCategories.list_items[21].code", equalTo(NOTICES_OF_ACTING_DISCHARGE),
                  "data.manageDocuments[0].value.documentCategories.list_items[22].code", equalTo(REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED),
                  "data.manageDocuments[0].value.documentCategories.list_items[23].code", equalTo(WITNESS_AVAILABILITY),
                  "data.manageDocuments[0].value.documentCategories.list_items[24].code", equalTo(LETTERS_OF_COMPLAINTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[25].code", equalTo(SPIP_REFERRAL_REQUESTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[26].code", equalTo(HOME_OFFICE_DWP_RESPONSES)
            )
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsEndPoint_thenRespWithCopiedDocuments() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenManageDocuments_GiveErrorWhenCourtAdminUserSelectCourt() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_COURT_REQUEST);
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
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/submitted")
            .then()
            .assertThat().statusCode(200);
    }

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
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
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

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.legalProfQuarantineDocsList[0].value.document.document_filename", equalTo("Test doc1.pdf"))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.legalProfUploadDocListDocTab[0].value.categoryId",
                  equalTo(APPLICANT_APPLICATION),
                  "data.legalProfUploadDocListDocTab[0].value.categoryName",
                  equalTo(APPLICANT_APPLICATION_NAME),
                  "data.legalProfUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename",
                  equalTo("Test doc2.pdf")
            )
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.cafcassQuarantineDocsList[0].value.cafcassQuarantineDocument.document_filename", equalTo("Test doc1.pdf"))
            .assertThat().statusCode(200);
    }

    @Test
    public void givenMangeDocs_whenCopyDocsNeitherConfNorRestricted_thenAppropriateCategoryForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.cafcassUploadDocListDocTab[0].value.categoryId", equalTo(APPLICANT_APPLICATION),
                  "data.cafcassUploadDocListDocTab[0].value.categoryName", equalTo(APPLICANT_APPLICATION_NAME),
                  "data.cafcassUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test doc2.pdf"))
            .assertThat().statusCode(200);

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCourtAdmin() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.restrictedDocuments[0].value.applicantApplicationDocument.document_filename", equalTo("Test doc4.pdf"))
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        System.out.println("MMMMM " + response);
    }

    @Test
    public void givenMangeDocs_whenCopyDocsNeitherConfNorRestricted_thenAppropriateCategoryForCourtAdmin() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED_ADMIN);

        String requestBodyRevised = requestBody
            .replace("\"isConfidential\": \"Yes\"",
                     "\"isConfidential\": \"No\"")
            .replace("\"isRestricted\": \"Yes\"",
                     "\"isRestricted\": \"No\"");

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .body("data.courtStaffUploadDocListDocTab[0].value.categoryId", equalTo(APPLICANT_APPLICATION),
                  "data.courtStaffUploadDocListDocTab[0].value.categoryName", equalTo(APPLICANT_APPLICATION_NAME),
                  "data.courtStaffUploadDocListDocTab[0].value.applicantApplicationDocument.document_filename", equalTo("Test doc4.pdf"))
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
        System.out.println("MMMMM " + response);

    }

}
