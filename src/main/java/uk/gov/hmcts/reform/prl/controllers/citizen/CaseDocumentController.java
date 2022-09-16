package uk.gov.hmcts.reform.prl.controllers.citizen;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DeleteDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;

@Slf4j
@RestController
public class CaseDocumentController {

    private static final String SERVICE_AUTH = "ServiceAuthorization";

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @Autowired
    private AuthorisationService authorisationService;

    Integer fileIndex;

    @PostMapping(path = "/generate-citizen-statement-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Generate a PDF for citizen as part of upload documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity generateCitizenStatementDocument(@RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                   @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        fileIndex = 0;
        String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey("documentType")
            && generateAndUploadDocumentRequest.getValues().containsKey("partyName")) {
            final String documentType = generateAndUploadDocumentRequest.getValues().get("documentType");
            final String partyName = generateAndUploadDocumentRequest.getValues().get("partyName");
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                tempCaseData.getCitizenUploadedDocumentList()
                    .stream().forEach((document) -> {
                        if (documentType.equalsIgnoreCase(document.getValue().getDocumentType())
                            && partyName.equalsIgnoreCase(document.getValue().getPartyName())) {
                            fileIndex++;
                        }
                    });
            }
        }
        UploadedDocuments uploadedDocuments =
            documentGenService.generateCitizenStatementDocument(
                authorisation,
                generateAndUploadDocumentRequest,
                fileIndex + 1
            );
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        if (uploadedDocuments != null) {
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
            } else {
                uploadedDocumentsList = new ArrayList<>();
            }
            Element<UploadedDocuments> uploadDocumentElement = ElementUtils.element(uploadedDocuments);
            uploadedDocumentsList.add(uploadDocumentElement);

            CaseData caseData = CaseData.builder().id(Long.valueOf(caseId))
                .citizenUploadedDocumentList(uploadedDocumentsList).build();
            caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                CITIZEN_UPLOADED_DOCUMENT
            );
            //return uploadedDocuments.getCitizenDocument().getDocumentFileName();
            return ResponseEntity.status(HttpStatus.OK).body(
                DocumentDetails.builder().documentId(uploadDocumentElement.getId().toString())
                    .documentName(uploadedDocuments.getCitizenDocument().getDocumentFileName()).build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.ordinal()).body(new DocumentDetails());
        }

    }


    @PostMapping(path = "/upload-citizen-statement-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON)
    @Operation(description = "Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while uploading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authroization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> uploadCitizenStatementDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                            @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                            @RequestParam("file") MultipartFile file) {

        if (!isAuthorized(authorisation, serviceAuthorization)) {
            throw (new RuntimeException("Invalid Client"));
        }
        return ResponseEntity.ok(documentGenService.uploadDocument(authorisation, file));
    }

    @PostMapping(path = "/delete-citizen-statement-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Delete a PDF for citizen as part of upload documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String deleteCitizenStatementDocument(@RequestBody DeleteDocumentRequest deleteDocumentRequest,
                                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                           @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        List<Element<UploadedDocuments>> tempUploadedDocumentsList;
        List<Element<UploadedDocuments>> uploadedDocumentsList = new ArrayList<>();
        String caseId = deleteDocumentRequest.getValues().get("caseId");
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (deleteDocumentRequest.getValues() != null
            && deleteDocumentRequest.getValues().containsKey("documentId")) {
            final String documenIdToBeDeleted = deleteDocumentRequest.getValues().get("documentId");
            log.info("Document to be deleted with id : " + documenIdToBeDeleted);
            tempUploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
            /*for (Element<UploadedDocuments> element : tempUploadedDocumentsList) {
                if (!documenIdToBeDeleted.equalsIgnoreCase(
                    element.getId().toString())) {
                    uploadedDocumentsList.add(element);
                }
            }*/
            uploadedDocumentsList = tempUploadedDocumentsList.stream().filter(element -> !documenIdToBeDeleted.equalsIgnoreCase(
                    element.getId().toString()))
                .collect(Collectors.toList());
        }
        log.info("uploadedDocumentsList::" + uploadedDocumentsList.size());
        CaseData caseData = CaseData.builder().id(Long.valueOf(caseId))
            .citizenUploadedDocumentList(uploadedDocumentsList).build();
        caseService.updateCase(
            caseData,
            authorisation,
            s2sToken,
            caseId,
            CITIZEN_UPLOADED_DOCUMENT
        );
        return "SUCCESS";
    }

    @DeleteMapping("/{documentId}/delete")
    @Operation(description = "Delete a document from client document api")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted document successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request while deleting the document"),
            @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<?> deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                            @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                            @PathVariable("documentId") String documentId) {
        if (!isAuthorized(authorisation, serviceAuthorization)) {
            throw (new RuntimeException("Invalid Client"));
        }
        return ResponseEntity.ok(documentGenService.deleteDocument(authorisation, documentId));
    }

    private boolean isAuthorized(String authorisation, String serviceAuthorization) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
                authorisationService.authoriseService(serviceAuthorization));
    }

}

