package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DeleteDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.UploadDocumentEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_ID;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseDocumentController {

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    private UploadDocumentService uploadService;

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    CaseService caseService;

    Integer fileIndex;

    @Autowired
    private EmailService emailService;

    @Value("${citizen.url}")
    private String dashboardUrl;

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
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey("documentType")
            && generateAndUploadDocumentRequest.getValues().containsKey("partyName")) {
            final String documentType = generateAndUploadDocumentRequest.getValues().get("documentType");
            final String partyName = generateAndUploadDocumentRequest.getValues().get("partyName");
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                tempCaseData.getCitizenUploadedDocumentList()
                    .stream().forEach(document -> {
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
        return getUploadedDocumentsList(
            generateAndUploadDocumentRequest,
            authorisation,
            s2sToken,
            caseId,
            tempCaseData,
            uploadedDocuments
        );

    }

    private ResponseEntity getUploadedDocumentsList(@RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                    @RequestHeader("serviceAuthorization") String s2sToken,
                                                    String caseId,
                                                    CaseData tempCaseData,
                                                    UploadedDocuments uploadedDocuments) {
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        if (uploadedDocuments != null) {
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
            } else {
                uploadedDocumentsList = new ArrayList<>();
            }
            Element<UploadedDocuments> uploadDocumentElement = element(uploadedDocuments);
            uploadedDocumentsList.add(uploadDocumentElement);

            CaseData caseData = CaseData.builder().id(Long.valueOf(caseId))
                .citizenUploadedDocumentList(uploadedDocumentsList).build();
            caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                CITIZEN_UPLOADED_DOCUMENT,
                ""
            );

            final String partyId = generateAndUploadDocumentRequest.getValues().get(PARTY_ID);
            notifyOtherParties(partyId, tempCaseData);

            return ResponseEntity.status(HttpStatus.OK).body(
                DocumentDetails.builder().documentId(uploadDocumentElement.getId().toString())
                    .documentName(uploadedDocuments.getCitizenDocument().getDocumentFileName()).build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.ordinal()).body(new DocumentDetails());
        }
    }

    private void notifyOtherParties(String partyId, CaseData tempCaseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(tempCaseData.getCaseTypeOfApplication())) {
            tempCaseData.getRespondents().forEach(element ->
                                                      findAndSendEmail(partyId, tempCaseData, element.getValue())
            );
            tempCaseData.getApplicants().forEach(element ->
                                                     findAndSendEmail(partyId, tempCaseData, element.getValue())
            );
        } else {
            findAndSendEmail(partyId, tempCaseData, tempCaseData.getRespondentsFL401());
            findAndSendEmail(partyId, tempCaseData, tempCaseData.getApplicantsFL401());
        }
    }

    private void findAndSendEmail(String partyId, CaseData tempCaseData, PartyDetails partyDetails) {
        if (partyDetails.getUser() != null && !partyId.equalsIgnoreCase(partyDetails.getUser().getIdamId())) {
            String email = partyDetails.getEmail();
            sendEmailToCitizen(tempCaseData, partyDetails.getFirstName(), email);
        }
    }

    private void sendEmailToCitizen(CaseData tempCaseData, String name, String email) {
        emailService.send(
            email,
            EmailTemplateNames.DOCUMENT_UPLOADED,
            buildUploadDocuemntEmail(tempCaseData, name, dashboardUrl),
            LanguagePreference.english
        );
    }

    private EmailTemplateVars buildUploadDocuemntEmail(CaseData caseData, String name, String link) {
        return UploadDocumentEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .name(name)
            .dashboardLink(link)
            .build();
    }

    @PostMapping(path = "/upload-citizen-statement-document", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while uploading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authroization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity uploadCitizenStatementDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                         @RequestHeader("serviceAuthorization") String s2sToken,
                                                         @ModelAttribute UploadedDocumentRequest uploadedDocumentRequest) {

        String caseId = uploadedDocumentRequest.getCaseId();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        UploadedDocuments uploadedDocuments = uploadService.uploadCitizenDocument(
            authorisation,
            uploadedDocumentRequest,
            caseId
        );
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        if (uploadedDocuments != null) {

            Element<UploadedDocuments> uploadedDocsElement = element(uploadedDocuments);
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
                uploadedDocumentsList.add(uploadedDocsElement);
            } else {
                uploadedDocumentsList = new ArrayList<>();
                uploadedDocumentsList.add(uploadedDocsElement);
            }
            CaseData caseData = CaseData.builder()
                .id(Long.parseLong(caseId))
                .citizenUploadedDocumentList(uploadedDocumentsList)
                .build();

            StartEventResponse startEventResponse =
                coreCaseDataApi.startEventForCitizen(
                    authorisation,
                    s2sToken,
                    idamClient.getUserInfo(authorisation).getUid(),
                    JURISDICTION,
                    CASE_TYPE,
                    caseId,
                    CITIZEN_UPLOADED_DOCUMENT
                );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(caseData).build();

            coreCaseDataApi.submitEventForCitizen(
                authorisation,
                s2sToken,
                idamClient.getUserInfo(authorisation).getUid(),
                JURISDICTION,
                CASE_TYPE,
                caseId,
                true,
                caseDataContent
            );

            final String partyId = uploadedDocumentRequest.getPartyId();
            notifyOtherParties(partyId, tempCaseData);

            return ResponseEntity.ok().body(
                DocumentDetails.builder().documentId(uploadedDocsElement.getId().toString())
                    .documentName(uploadedDocuments.getCitizenDocument().getDocumentFileName()).build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.ordinal()).body(new DocumentDetails());
        }
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
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (deleteDocumentRequest.getValues() != null
            && deleteDocumentRequest.getValues().containsKey("documentId")) {
            final String documenIdToBeDeleted = deleteDocumentRequest.getValues().get("documentId");
            tempUploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
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
            CITIZEN_UPLOADED_DOCUMENT,
            ""
        );
        return "SUCCESS";
    }

    @PostMapping(path = "/upload-citizen-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
        APPLICATION_JSON)
    @Operation(description = "Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while uploading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authroization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> uploadCitizenDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader("ServiceAuthorization") String serviceAuthorization,
                                                   @RequestParam("file") MultipartFile file) throws IOException {

        if (!isAuthorized(authorisation, serviceAuthorization)) {
            throw (new RuntimeException("Invalid Client"));
        }
        return ResponseEntity.ok(documentGenService.uploadDocument(authorisation, file));
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
                                            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
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

