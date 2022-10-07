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
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EVENT_CITIZEN_RESPONSE_C7_DOCUMENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


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

    @Autowired
    CaseService caseService;

    @Autowired
    CaseController caseController;

    Integer fileIndex;

    @PostMapping(path = "/generate-citizen-respond-to-application-c7document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Generate a PDF for citizen as part of Respond to the Application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public CaseData generateCitizenRespondToApplicationC7Document(
        @RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        log.info("generateCitizenRespondToApplicationC7Document User roles: {} ", idamClient.getUserDetails(authorisation).getRoles());
        log.info("generateCitizenRespondToApplicationC7Document xUser details: {} ", idamClient.getUserDetails(authorisation));
        fileIndex = 0;
        String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        CaseDetails caseDetailsReturn = null;

        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey("documentType")
            && generateAndUploadDocumentRequest.getValues().containsKey("partyName")) {
            final String documentType = generateAndUploadDocumentRequest.getValues().get("documentType");
            final String partyName = generateAndUploadDocumentRequest.getValues().get("partyName");

            if (tempCaseData.getCitizenResponseC7DocumentList() != null
                && !tempCaseData.getCitizenResponseC7DocumentList().isEmpty()) {
                tempCaseData.getCitizenResponseC7DocumentList()
                    .stream().forEach((document) -> {
                        if (documentType.equalsIgnoreCase(document.getValue().getDocumentType())
                            && partyName.equalsIgnoreCase(document.getValue().getPartyName())) {
                            fileIndex++;
                        }
                    });
            }
        }

        log.info("BEFORE call to generate Document " + caseId);
        ResponseDocuments responseDocuments =
            documentGenService.generateCitizenResponseDocument(
                authorisation,
                generateAndUploadDocumentRequest,
                fileIndex + 1,
                tempCaseData
            );
        log.info("AFTER call to generate Document " + caseId);

        List<Element<ResponseDocuments>> responseDocumentsList;
        if (responseDocuments != null) {
            if (tempCaseData.getCitizenResponseC7DocumentList() != null
                && !tempCaseData.getCitizenResponseC7DocumentList().isEmpty()) {
                responseDocumentsList = tempCaseData.getCitizenResponseC7DocumentList();
            } else {
                responseDocumentsList = (List) new java.util.ArrayList<>();
            }
            Element<ResponseDocuments> responseDocumentElement = element(responseDocuments);
            responseDocumentsList.add(responseDocumentElement);

            log.info("Amending the Case Data with citizenResponseC7DocumentList " + caseId);
            CaseData caseData = CaseData.builder().id(Long.valueOf(caseId))
                .citizenResponseC7DocumentList(responseDocumentsList).build();

            log.info("Call updateCase with event " + EVENT_CITIZEN_RESPONSE_C7_DOCUMENT + " for case id " + caseId);
            caseDetailsReturn = caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                EVENT_CITIZEN_RESPONSE_C7_DOCUMENT

            );

        }
        return objectMapper.convertValue(
            caseDetailsReturn.getData(),
            CaseData.class
        );

    }

}

