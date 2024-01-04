package uk.gov.hmcts.reform.prl.controllers.managedocuments;


import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPROVED_ORDERS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CASE_SUMMARY;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.COURT_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_EXPERT_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DNA_REPORTS_OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.DRUG_AND_ALCOHOL_TEST;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.GUARDIAN_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.HOME_OFFICE_DWP_RESPONSES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.JUDGE_NOTES_FROM_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LETTERS_OF_COMPLAINTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MAGISTRATES_FACTS_AND_REASONS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICES_OF_ACTING_DISCHARGE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_DOCS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_DISCLOSURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POLICE_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PRIVACY_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PUBLIC_FUNDING_CERTIFICATES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESULTS_OF_HAIR_STRAND_BLOOD_TESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SAFEGUARDING_LETTER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SEC37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION7_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_37_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SIXTEEN_A_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_GUARDIANSHIP_REPORT;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPECIAL_MEASURES;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SPIP_REFERRAL_REQUESTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.STANDARD_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS;
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
    private static final String MANAGE_DOCUMENT_COURT_REQUEST =
        "requests/manage-documents-request-with-court-as-party.json";
    //private static final String MANAGE_DOCUMENT_RESPONSE = "response/MangeDocument.json";

    private static final String MANAGE_DOCUMENT_REQUEST_RESTRICTED = "requests/manage-documents-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NOT_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private static final String MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED = "requests/manage-documents-not-restricted.json";

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenCaseId_whenAboutToStartEndPoint_thenRespWithDocumentCategories() throws Exception {

        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        //String response = ResourceLoader.loadJson(MANAGE_DOCUMENT_RESPONSE);
        //JSONObject jsObject = new JSONObject(response);
        request
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
                  "data.manageDocuments[0].value.documentCategories.list_items[3].code", equalTo(APPLICATIONS_WITHIN_PROCEEDINGS),
                  "data.manageDocuments[0].value.documentCategories.list_items[4].code", equalTo(MIAM_CERTIFICATE),
                  "data.manageDocuments[0].value.documentCategories.list_items[5].code", equalTo(PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[6].code", equalTo(RESPONDENT_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[7].code", equalTo(RESPONDENT_C1A_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[8].code", equalTo(RESPONDENT_C1A_RESPONSE),
                  "data.manageDocuments[0].value.documentCategories.list_items[9].code", equalTo(APPLICATIONS_FROM_OTHER_PROCEEDINGS),
                  "data.manageDocuments[0].value.documentCategories.list_items[10].code", equalTo(NOTICE_OF_HEARING),
                  "data.manageDocuments[0].value.documentCategories.list_items[11].code", equalTo(COURT_BUNDLE),
                  "data.manageDocuments[0].value.documentCategories.list_items[12].code", equalTo(CASE_SUMMARY),
                  "data.manageDocuments[0].value.documentCategories.list_items[13].code", equalTo(SAFEGUARDING_LETTER),
                  "data.manageDocuments[0].value.documentCategories.list_items[14].code", equalTo(SECTION7_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[15].code", equalTo(SECTION_37_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[16].code", equalTo(SIXTEEN_A_RISK_ASSESSMENT),
                  "data.manageDocuments[0].value.documentCategories.list_items[17].code", equalTo(GUARDIAN_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[18].code", equalTo(SPECIAL_GUARDIANSHIP_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[19].code", equalTo(OTHER_DOCS),
                  "data.manageDocuments[0].value.documentCategories.list_items[20].code", equalTo(CONFIDENTIAL),
                  "data.manageDocuments[0].value.documentCategories.list_items[21].code", equalTo(EMAILS_TO_COURT_TO_REQUEST_HEARINGS_ADJOURNED),
                  "data.manageDocuments[0].value.documentCategories.list_items[22].code", equalTo(PUBLIC_FUNDING_CERTIFICATES),
                  "data.manageDocuments[0].value.documentCategories.list_items[23].code", equalTo(NOTICES_OF_ACTING_DISCHARGE),
                  "data.manageDocuments[0].value.documentCategories.list_items[24].code", equalTo(REQUEST_FOR_FAS_FORMS_TO_BE_CHANGED),
                  "data.manageDocuments[0].value.documentCategories.list_items[25].code", equalTo(WITNESS_AVAILABILITY),
                  "data.manageDocuments[0].value.documentCategories.list_items[26].code", equalTo(LETTERS_OF_COMPLAINTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[27].code", equalTo(SPIP_REFERRAL_REQUESTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[28].code", equalTo(HOME_OFFICE_DWP_RESPONSES),
                  "data.manageDocuments[0].value.documentCategories.list_items[29].code", equalTo("bulkScanQuarantine"),
                  "data.manageDocuments[0].value.documentCategories.list_items[30].code", equalTo(MEDICAL_REPORTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[31].code", equalTo(DNA_REPORTS_EXPERT_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[32].code", equalTo(RESULTS_OF_HAIR_STRAND_BLOOD_TESTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[33].code", equalTo(POLICE_DISCLOSURES),
                  "data.manageDocuments[0].value.documentCategories.list_items[34].code", equalTo(MEDICAL_RECORDS),
                  "data.manageDocuments[0].value.documentCategories.list_items[35].code", equalTo(DRUG_AND_ALCOHOL_TEST),
                  "data.manageDocuments[0].value.documentCategories.list_items[36].code", equalTo(POLICE_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[37].code", equalTo(SEC37_REPORT),
                  "data.manageDocuments[0].value.documentCategories.list_items[38].code", equalTo(ORDERS_SUBMITTED_WITH_APPLICATION),
                  "data.manageDocuments[0].value.documentCategories.list_items[39].code", equalTo(APPROVED_ORDERS),
                  "data.manageDocuments[0].value.documentCategories.list_items[40].code", equalTo(STANDARD_DIRECTIONS_ORDER),
                  "data.manageDocuments[0].value.documentCategories.list_items[41].code", equalTo(TRANSCRIPTS_OF_JUDGEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[42].code", equalTo(MAGISTRATES_FACTS_AND_REASONS),
                  "data.manageDocuments[0].value.documentCategories.list_items[43].code", equalTo(JUDGE_NOTES_FROM_HEARING),
                  "data.manageDocuments[0].value.documentCategories.list_items[44].code", equalTo(IMPORTANT_INFO_ABOUT_ADDRESS_AND_CONTACT),
                  "data.manageDocuments[0].value.documentCategories.list_items[45].code", equalTo(DNA_REPORTS_OTHER_DOCS),
                  "data.manageDocuments[0].value.documentCategories.list_items[46].code", equalTo(PRIVACY_NOTICE),
                  "data.manageDocuments[0].value.documentCategories.list_items[47].code", equalTo(SPECIAL_MEASURES),
                  "data.manageDocuments[0].value.documentCategories.list_items[48].code", equalTo(ANY_OTHER_DOC),
                  "data.manageDocuments[0].value.documentCategories.list_items[49].code", equalTo(POSITION_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[50].code", equalTo(APPLICANT_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[51].code", equalTo(RESPONDENT_STATEMENTS),
                  "data.manageDocuments[0].value.documentCategories.list_items[52].code", equalTo(OTHER_WITNESS_STATEMENTS)


            )
            .assertThat().statusCode(200);

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
            .post("/manage-documents/validate-court-user")
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
            .post("/manage-documents/validate-court-user")
            .then()
            .body("data.caseTypeOfApplication", equalTo("C100"))
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
        AboutToStartOrSubmitCallbackResponse response =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs-mid")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(0,response.getErrors().size());

    }

    @Test
    public void givenManageDocuments_whenCopy_manage_docsMid_thenCheckDocumentField_WhenRestricted() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);
        AboutToStartOrSubmitCallbackResponse response =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs-mid")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertEquals(1,response.getErrors().size());
        Assert.assertEquals(DETAILS_ERROR_MESSAGE,response.getErrors().get(0));
    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);


        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("legalProfQuarantineDocsList");

        Assert.assertNotNull(legalProfQuarantineDocsList);
        Assert.assertEquals(1,legalProfQuarantineDocsList.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForSolicitor() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(legalProfUploadDocListDocTab);
        Assert.assertEquals(1,legalProfUploadDocListDocTab.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("cafcassQuarantineDocsList");

        Assert.assertNotNull(cafcassQuarantineDocsList);
        Assert.assertEquals(1,cafcassQuarantineDocsList.size());

    }

    @Test
    public void givenMangeDocs_whenCopyDocs_thenRespWithCopiedDocuments_whenNeitherConfNorRestricedForCafcass() throws Exception {
        String requestBody = ResourceLoader.loadJson(MANAGE_DOCUMENT_REQUEST_NEITHER_CONF_NOR_RESTRICTED);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/manage-documents/copy-manage-docs")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab
            = (List<Element<QuarantineLegalDoc>>) response.getData().get("cafcassUploadDocListDocTab");

        Assert.assertNotNull(cafcassUploadDocListDocTab);
        Assert.assertEquals(1,cafcassUploadDocListDocTab.size());

    }


}
