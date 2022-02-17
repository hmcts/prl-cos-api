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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.ExampleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.workflows.ApplicationConsiderationTimetableValidationWorkflow;
import uk.gov.hmcts.reform.prl.workflows.ValidateMiamApplicationOrExemptionWorkflow;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CallbackController {

    private static final String DRAFT_C_100_APPLICATION = "Draft_C100_application.pdf";
    public static final String PRL_DRAFT_TEMPLATE = "PRL-DRAFT-C100-20.docx";
    private static final String C8_DOC = "C8Document.pdf";
    private static final String C100_FINAL_DOC = "C100FinalDocument.docx";
    private static final String C100_FINAL_TEMPLATE = "c100-final-template-1.docx";
    public static final String PRL_C8_TEMPLATE = "PRL-C8-Final-Changes.docx";
    public static final String PRL_C1A_TEMPLATE = "PRL-C1A.docx";
    public static final String PRL_C1A_FILENAME = "C1A_Document.pdf";
    private final ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;
    private final ExampleService exampleService;
    private final OrganisationService organisationService;
    private final ValidateMiamApplicationOrExemptionWorkflow validateMiamApplicationOrExemptionWorkflow;

    private final DgsService dgsService;
    private final ObjectMapper objectMapper;


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
    public uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse generateAndStoreDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody @ApiParam("CaseData") CallbackRequest request
    ) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            request.getCaseDetails(),
            PRL_DRAFT_TEMPLATE
        );
        return uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse
            .builder()
            .data(CaseData.builder().draftOrderDoc(Document.builder()
                                                       .documentUrl(generatedDocumentInfo.getUrl())
                                                       .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                       .documentHash(generatedDocumentInfo.getHashToken())
                                                       .documentFileName(DRAFT_C_100_APPLICATION).build()).build())
            .build();
    }

    @PostMapping(path = "/generate-c8-c1a-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to generate and store document")
    public AboutToStartOrSubmitCallbackResponse generateC8AndOtherDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
            .toBuilder()
            .id(callbackRequest.getCaseDetails().getId())
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            PRL_C8_TEMPLATE
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("Generate C1A if allegations of harm is set to Yes and the passed value is {}",
                 caseData.getAllegationsOfHarmYesNo());
        if (caseData.getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
            GeneratedDocumentInfo generatedC1ADocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
                PRL_C1A_TEMPLATE
            );
            caseDataUpdated.put("c1ADocument", Document.builder()
                .documentUrl(generatedC1ADocumentInfo.getUrl())
                .documentBinaryUrl(generatedC1ADocumentInfo.getBinaryUrl())
                .documentHash(generatedC1ADocumentInfo.getHashToken())
                .documentFileName(PRL_C1A_FILENAME).build());
        }
        caseDataUpdated.put("c8Document", Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(C8_DOC).build());

        GeneratedDocumentInfo generatedDocumentInfoFinal = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            C100_FINAL_TEMPLATE
        );

        caseDataUpdated.put("finalDocument", Document.builder()
            .documentUrl(generatedDocumentInfoFinal.getUrl())
            .documentBinaryUrl(generatedDocumentInfoFinal.getBinaryUrl())
            .documentHash(generatedDocumentInfoFinal.getHashToken())
            .documentFileName(C100_FINAL_DOC).build());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/save-organisation-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to retrieve and store organisation details")
    public AboutToStartOrSubmitCallbackResponse saveOrganisationDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody @ApiParam("CaseData") uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) throws Exception {

        log.info("=====***** Case Data from CCD before callback *****====== {}", callbackRequest.getCaseDetails().getData());

        CaseData caseData = objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)
            .toBuilder()
            .id(callbackRequest.getCaseDetails().getId())
            .build();

      //  caseData = organisationService.getApplicantOrganisationDetails(caseData);

      //  caseData = organisationService.getRespondentOrganisationDetails(caseData);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        caseDataUpdated.put("data", caseData);

        log.info("======== CAseData with applicant Organisation ~Details==== {}",caseData);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(caseDataUpdated)
            .build();
    }
}
