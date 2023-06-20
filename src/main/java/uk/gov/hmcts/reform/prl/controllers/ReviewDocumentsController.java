package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments.reviewDocTempFields;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewDocumentsController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewDocumentService reviewDocumentService;

    @PostMapping(path = "/review-documents/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = reviewDocumentService.getDynamicListElements(caseData);

        if (dynamicListElements.isEmpty()) {
            errors = List.of("No documents to review");
        }
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        caseDataUpdated.put("reviewDocsDynamicList", DynamicList.builder().listItems(dynamicListElements).build());

        //clear the previous decision
        CaseUtils.removeTemporaryFields(caseDataUpdated, "reviewDecisionYesOrNo");

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).errors(errors).build();
    }

    @PostMapping(path = "/review-documents/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        reviewDocumentService.getReviewedDocumentDetails(caseData, caseDataUpdated);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/review-documents/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) throws Exception {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info("*************************** BEFORE REVIEW ***************************");
        log.info("*** Legal prof docs q ** {}", caseData.getLegalProfQuarantineDocsList());
        log.info("*** Cafcass quarantine docs ** {}", caseData.getCafcassQuarantineDocsList());
        log.info("***citizen docs q ** {}", caseData.getCitizenUploadQuarantineDocsList());
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
        reviewDocumentService.processReviewDocument(caseDataUpdated, caseData, uuid);
        log.info("*************************** AFTER REVIEW ***************************");
        log.info("*** Legal prof docs q ** {}", caseData.getLegalProfQuarantineDocsList());
        log.info("*** Cafcass quarantine docs ** {}", caseData.getCafcassQuarantineDocsList());
        log.info("***citizen docs q ** {}", caseData.getCitizenUploadQuarantineDocsList());

        caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        caseDataUpdated.put("cafcassQuarantineDocsList", caseData.getCafcassQuarantineDocsList());
        caseDataUpdated.put("citizenUploadQuarantineDocsList", caseData.getCitizenUploadQuarantineDocsList());

        if (caseData.getLegalProfQuarantineDocsList().isEmpty()
            || caseData.getCitizenUploadQuarantineDocsList().isEmpty()
            || caseData.getCafcassQuarantineDocsList().isEmpty()) {
            caseDataUpdated.put("allDocumentsReviewedFlag", "True");
        }

        //clear fields
        CaseUtils.removeTemporaryFields(caseDataUpdated, reviewDocTempFields());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/review-documents/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(@RequestHeader("Authorization")
                                                                     @Parameter(hidden = true) String authorisation,
                                                                     @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        return reviewDocumentService.getReviewResult(caseData);
    }
}
