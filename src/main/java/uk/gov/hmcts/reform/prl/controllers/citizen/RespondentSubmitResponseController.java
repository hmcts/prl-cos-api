package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentDetails;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RestController
public class RespondentSubmitResponseController {

    @Autowired
    CaseService caseService;

    @Autowired
    DocumentGenService documentGenService;

    @PostMapping(value = "{caseId}/{eventId}/respondent-submit-response", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Respondent submit response")
    public ResponseEntity submitRespondentResponse(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) throws Exception {

        UploadedDocuments uploadedDocuments = documentGenService.generateC7FinalDocument(authorisation, caseData);
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        if (uploadedDocuments != null) {
            if (caseData.getCitizenUploadedDocumentList() != null
                    && !caseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = caseData.getCitizenUploadedDocumentList();
            } else {
                uploadedDocumentsList = new ArrayList<>();
            }

            Element<UploadedDocuments> uploadDocumentElement = element(uploadedDocuments);
            uploadedDocumentsList.add(uploadDocumentElement);

            caseData = CaseData.builder().id(Long.valueOf(caseId))
                .citizenUploadedDocumentList(uploadedDocumentsList).build();


            caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                CITIZEN_UPLOADED_DOCUMENT
            );

            return ResponseEntity.status(HttpStatus.OK).body(
                DocumentDetails.builder().documentId(uploadDocumentElement.getId().toString())
                    .documentName(uploadedDocuments.getCitizenDocument().getDocumentFileName()).build());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.ordinal()).body(new DocumentDetails());
        }

    }
}
