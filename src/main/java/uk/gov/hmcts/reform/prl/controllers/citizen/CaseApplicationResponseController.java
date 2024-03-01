package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseApplicationResponseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApplicationResponseController {
    private final DocumentGenService documentGenService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;
    private final CitizenResponseNotificationEmailService citizenResponseNotificationEmailService;

    private final CaseApplicationResponseService caseApplicationResponseService;

    @PostMapping(path = "/{caseId}/{partyId}/generate-c7document", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public Document generateC7DraftDocument(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseApplicationResponseService.updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
        log.info(" Generating C7 draft document for respondent ");

        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DOCUMENT_C7_DRAFT_HINT,
            false
        );
        log.info("C7 draft document generated successfully for respondent ");
        return document;
    }

    @PostMapping(path = "/{caseId}/{partyId}/generate-c7document-final", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public CaseData generateC7FinalDocument(
        @PathVariable("caseId") String caseId,
        @PathVariable("partyId") String partyId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseApplicationResponseService.updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);

        CaseDetails caseDetailsReturn = caseApplicationResponseService.generateCitizenResponseFinalDocuments(caseData, caseDetails,
            authorisation, partyId, caseId, s2sToken);

        if (caseDetailsReturn != null) {
            log.info("generateC7FinalDocument:: sending notification to applicant solicitor");
            citizenResponseNotificationEmailService.sendC100ApplicantSolicitorNotification(caseDetails);
            return objectMapper.convertValue(
                caseDetailsReturn.getData(),
                CaseData.class
            );
        }

        return objectMapper.convertValue(
            caseData,
            CaseData.class
        );
    }
}

