package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;


@Slf4j
@RestController
public class CaseApplicationResponseController {

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private IdamClient idamClient;


    Integer fileIndex;

    @PostMapping(path = "/generate-citizen-application-response-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity generateCitizenApplicationResponseDocument(@RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                                     @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        log.info("User roles: {} ", idamClient.getUserDetails(authorisation).getRoles());
        log.info("User details: {} ", idamClient.getUserDetails(authorisation));
        fileIndex = 0;
        String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataMap = tempCaseData.toMap(objectMapper);

        if (C100_CASE_TYPE.equalsIgnoreCase(tempCaseData.getCaseTypeOfApplication())) {
            caseDataMap.putAll(documentGenService.generateDocumentsRespondToApplication(authorisation, tempCaseData));
        }

        if (caseDataMap.get(DRAFT_DOCUMENT_FIELD) != null || caseDataMap.get(DRAFT_DOCUMENT_WELSH_FIELD) != null) {
            // make a call to the dgs service to generate the pdf document //
            return ResponseEntity.status(HttpStatus.OK).body("Generate PDF Successful");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.ordinal()).body("Generate PDF Failed");
        }

    }

}

