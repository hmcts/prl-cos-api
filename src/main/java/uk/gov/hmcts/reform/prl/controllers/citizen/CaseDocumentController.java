package uk.gov.hmcts.reform.prl.controllers.citizen;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;

@Slf4j
@RestController
public class CaseDocumentController {

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @PostMapping(path = "/generate-citizen-statement-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Generate a PDF for citizen as part of upload documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String generateCitizenStatementDocument(@RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        UploadedDocuments uploadedDocuments = documentGenService.generateCitizenStatementDocument(authorisation, generateAndUploadDocumentRequest);
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        if (uploadedDocuments != null) {
            String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
            CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);

            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            if (caseData.getCitizenUploadedDocumentList() != null && !caseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = caseData.getCitizenUploadedDocumentList();
            } else {
                uploadedDocumentsList = new ArrayList<>();
            }
            uploadedDocumentsList.add(Element.<UploadedDocuments>builder()
                                          .value(uploadedDocuments)
                                          .build());
            caseData = CaseData.builder().citizenUploadedDocumentList(uploadedDocumentsList).build();
            caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                CITIZEN_UPLOADED_DOCUMENT
            );
            return uploadedDocuments.getCitizenDocument().getDocumentFileName();
        } else {
            return "FAILED";
        }

    }

}

