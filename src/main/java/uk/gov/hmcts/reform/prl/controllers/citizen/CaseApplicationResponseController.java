package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REVIEW_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseApplicationResponseController {

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @Autowired
    CitizenResponseNotificationEmailService citizenResponseNotificationEmailService;


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
        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
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
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
        log.info(" Generating C7 Final document for respondent ");
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C7_FINAL_ENGLISH,
            false
        );
        log.info("C7 Final document generated successfully for respondent ");
        updateCurrentRespondent(caseData, null, partyId);
        CaseDetails caseDetailsReturn = null;
        List<Element<ResponseDocuments>> responseDocumentsList = new ArrayList<>();
        if (document != null) {
            if (caseData.getCitizenResponseC7DocumentList() != null) {
                responseDocumentsList.addAll(caseData.getCitizenResponseC7DocumentList());
            }
            String partyName = caseData.getRespondents().stream().filter(element -> element.getId()
                    .toString().equalsIgnoreCase(partyId)).map(Element::getValue)
                .findFirst().map(partyDetails -> partyDetails.getLabelForDynamicList()).orElse("");

            Element<ResponseDocuments> responseDocumentElement = element(ResponseDocuments.builder()
                                                                             .partyName(partyName)
                                                                             .createdBy(partyId)
                                                                             .citizenDocument(document)
                                                                             .dateCreated(LocalDate.now())
                                                                             .build());
            responseDocumentsList.add(responseDocumentElement);
            caseData = caseData.toBuilder().citizenResponseC7DocumentList(responseDocumentsList).build();
            caseDetailsReturn = caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                REVIEW_AND_SUBMIT,
                null
            );
        }

        if (caseDetailsReturn != null) {
            /**
             * send notification to Applicant solicitor for respondent's response
             */
            log.info("generateC7FinalDocument:: sending notification to applicant solicitor ***** ");
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

    private CaseData updateCurrentRespondent(CaseData caseData, YesOrNo currentRespondent, String partyId) {

        for (Element<PartyDetails> partyElement: caseData.getRespondents()) {
            if (partyElement.getId().toString().equalsIgnoreCase(partyId)) {
                PartyDetails respondent = partyElement.getValue();
                respondent.setCurrentRespondent(currentRespondent);
            }
        }
        return caseData;
    }
}

