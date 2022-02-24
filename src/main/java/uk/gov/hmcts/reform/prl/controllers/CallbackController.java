package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CallbackController {

    private static final String DRAFT_C_100_APPLICATION = "Draft_C100_application.pdf";
    public static final String PRL_DRAFT_TEMPLATE = "PRL-C100-Draft-Final.docx";
    private static final String C8_DOC = "C8Document.pdf";
    private static final String C100_FINAL_DOC = "C100FinalDocument.pdf";
    private static final String C100_FINAL_TEMPLATE = "C100-Final-Document.docx";
    public static final String PRL_C8_TEMPLATE = "PRL-C8-Final-Changes.docx";
    public static final String PRL_C1A_TEMPLATE = "PRL-C1A.docx";
    public static final String PRL_C1A_FILENAME = "C1A_Document.pdf";
    public static final String C8_WELSH_FILENAME = "C8Document_Welsh.pdf";
    public static final String PRL_C8_WELSH_TEMPLATE = "PRL-C8-Welsh.docx";
    public static final String PRL_C1A_WELSH_TEMPLATE = "PRL-C1A-Welsh.docx";
    public static final String PRL_C1A_WELSH_FILENAME = "C1A_Document_Welsh.pdf";
    public static final String PRL_C100_DRAFT_WELSH_TEMPLATE = "PRL-Draft-C100-Welsh.docx";
    public static final String PRL_C100_DRAFT_WELSH_FILENAME = "Draft_C100_application_welsh.pdf";
    private static final String C100_FINAL_WELSH_FILENAME = "C100FinalDocumentWelsh.pdf";
    private static final String C100_FINAL_WELSH_TEMPLATE = "C100-Final-Document-Welsh.docx";
    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final ExampleService exampleService;
    private final OrganisationService organisationService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;
    private final SolicitorEmailService solicitorEmailService;

    private final DgsService dgsService;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final UserService userService;
    private final DocumentLanguageService documentLanguageService;


    /**
     * It's just an example - to be removed when there are real tasks sending emails.
     */
    @PostMapping(path = "/send-email", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to send email")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse> sendEmail(
        @RequestBody @ApiParam("CaseData") CallbackRequest request
    ) throws WorkflowException {
        return ok(
            uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.builder()
                .data(exampleService.executeExampleWorkflow(request.getCaseDetails()))
                .build()
        );
    }

    @PostMapping(path = "/validate-application-consideration-timetable", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to validate application consideration timetable. Returns error messages if validation fails.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateApplicationConsiderationTimetable(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = applicationConsiderationTimetableValidationWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()
        );
    }

    @PostMapping(path = "/validate-miam-application-or-exemption", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to confirm that a MIAM has been attended or applicant is exempt. Returns error message if confirmation fails")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<uk.gov.hmcts.reform.ccd.client.model.CallbackResponse> validateMiamApplicationOrExemption(
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws WorkflowException {
        WorkflowResult workflowResult = validateMiamApplicationOrExemptionWorkflow.run(callbackRequest);

        return ok(
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(workflowResult.getErrors())
                .build()

        );
    }


    @PostMapping(path = "/generate-save-draft-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate and store document")
    public AboutToStartOrSubmitCallbackResponse generateAndStoreDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody @ApiParam("CaseData") uk.gov.hmcts.reform.ccd.client.model.CallbackRequest request
    ) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(request.getCaseDetails(), objectMapper);
        //caseData = organisationService.getApplicantOrganisationDetails(caseData);
        //caseData = organisationService.getRespondentOrganisationDetails(caseData);

        Map<String, Object> caseDataUpdated = request.getCaseDetails().getData();

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        log.info("Based on Welsh Language requirement document generated will in English: {} and Welsh {}",
                 documentLanguage.isGenEng(),documentLanguage.isGenWelsh());


        if (documentLanguage.isGenEng()) {
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                PRL_DRAFT_TEMPLATE
            );

            caseDataUpdated.put("isEngDocGen", Yes.toString());
            caseDataUpdated.put("draftOrderDoc", Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(DRAFT_C_100_APPLICATION).build());
        }

        if (documentLanguage.isGenWelsh()) {
            GeneratedDocumentInfo generatedWelshDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                PRL_C100_DRAFT_WELSH_TEMPLATE
            );

            caseDataUpdated.put("isWelshDocGen", Yes.toString());
            caseDataUpdated.put("draftOrderDocWelsh", Document.builder()
                .documentUrl(generatedWelshDocumentInfo.getUrl())
                .documentBinaryUrl(generatedWelshDocumentInfo.getBinaryUrl())
                .documentHash(generatedWelshDocumentInfo.getHashToken())
                .documentFileName(PRL_C100_DRAFT_WELSH_FILENAME).build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/generate-c8-c1a-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate and store document")
    public AboutToStartOrSubmitCallbackResponse generateC8AndOtherDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseData.toBuilder().issueDate(LocalDate.now());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        log.info("Based on Welsh Language requirement document generated will in English: {} and Welsh {}",
                 documentLanguage.isGenEng(),documentLanguage.isGenWelsh());

        if (documentLanguage.isGenEng()) {
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                PRL_C8_TEMPLATE
            );

            log.info(
                "Generate C1A if allegations of harm is set to Yes and the passed value is {}",
                caseData.getAllegationsOfHarmYesNo()
            );
            if (caseData.getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
                GeneratedDocumentInfo generatedC1ADocumentInfo = dgsService.generateDocument(
                    authorisation,
                    uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                    PRL_C1A_TEMPLATE
                );
                caseDataUpdated.put(DOCUMENT_FIELD_C1A, Document.builder()
                    .documentUrl(generatedC1ADocumentInfo.getUrl())
                    .documentBinaryUrl(generatedC1ADocumentInfo.getBinaryUrl())
                    .documentHash(generatedC1ADocumentInfo.getHashToken())
                    .documentFileName(PRL_C1A_FILENAME).build());
            }
            caseDataUpdated.put(DOCUMENT_FIELD_C8, Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(C8_DOC).build());
            //caseData = organisationService.getApplicantOrganisationDetails(caseData);

            //caseData = organisationService.getRespondentOrganisationDetails(caseData);

            GeneratedDocumentInfo generatedDocumentInfoFinal = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                C100_FINAL_TEMPLATE
            );

            caseDataUpdated.put(DOCUMENT_FIELD_FINAL, Document.builder()
                .documentUrl(generatedDocumentInfoFinal.getUrl())
                .documentBinaryUrl(generatedDocumentInfoFinal.getBinaryUrl())
                .documentHash(generatedDocumentInfoFinal.getHashToken())
                .documentFileName(C100_FINAL_DOC).build());
        }

        if (documentLanguage.isGenWelsh()) {
            GeneratedDocumentInfo generatedC8WelshDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                PRL_C8_WELSH_TEMPLATE
            );
            caseDataUpdated.put(DOCUMENT_FIELD_C8_WELSH, Document.builder()
                .documentUrl(generatedC8WelshDocumentInfo.getUrl())
                .documentBinaryUrl(generatedC8WelshDocumentInfo.getBinaryUrl())
                .documentHash(generatedC8WelshDocumentInfo.getHashToken())
                .documentFileName(C8_WELSH_FILENAME).build());

            log.info("Generate C1A if allegations of harm is set to Yes and the passed value is {}",
                     caseData.getAllegationsOfHarmYesNo());
            if (caseData.getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
                GeneratedDocumentInfo generatedC1AWelshDocumentInfo = dgsService.generateDocument(
                    authorisation,
                    uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                    PRL_C1A_WELSH_TEMPLATE
                );
                caseDataUpdated.put(DOCUMENT_FIELD_C1A_WELSH, Document.builder()
                    .documentUrl(generatedC1AWelshDocumentInfo.getUrl())
                    .documentBinaryUrl(generatedC1AWelshDocumentInfo.getBinaryUrl())
                    .documentHash(generatedC1AWelshDocumentInfo.getHashToken())
                    .documentFileName(PRL_C1A_WELSH_FILENAME).build());
            }
            //caseData = organisationService.getApplicantOrganisationDetails(caseData);

            //caseData = organisationService.getRespondentOrganisationDetails(caseData);
            GeneratedDocumentInfo generatedFinalWelshDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                C100_FINAL_WELSH_TEMPLATE
            );

            caseDataUpdated.put(DOCUMENT_FIELD_FINAL_WELSH, Document.builder()
                .documentUrl(generatedFinalWelshDocumentInfo.getUrl())
                .documentBinaryUrl(generatedFinalWelshDocumentInfo.getBinaryUrl())
                .documentHash(generatedFinalWelshDocumentInfo.getHashToken())
                .documentFileName(C100_FINAL_WELSH_FILENAME).build());
        }

        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);

        caseDataUpdated.putAll(allTabsFields);
        caseDataUpdated.put("issueDate",caseData.getIssueDate());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/update-application", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to refresh the tabs")
    public void updateApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        allTabsService.updateAllTabs(caseData);
    }

    @PostMapping(path = "/case-withdrawn-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Send Email Notification on Case Withdraw")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnCaseWithdraw(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        UserDetails userDetails = userService.getUserDetails(authorisation);
        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            solicitorEmailService.sendEmailToSolicitor(caseDetails, userDetails);

            // Refreshing the page in the same event. Hence no external event call needed.
            // Getting the tab fields and add it to the casedetails..
            Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);

            caseDataUpdated.putAll(allTabsFields);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
