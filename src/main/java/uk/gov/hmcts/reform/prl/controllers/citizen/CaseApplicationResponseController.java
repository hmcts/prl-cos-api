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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseApplicationResponseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApplicationResponseController {
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
        @RequestBody DocumentRequest documentRequest,
        @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {

        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);

        return documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DOCUMENT_C7_DRAFT_HINT,
            false
        );
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

        updateCurrentRespondent(caseData, YesOrNo.Yes, partyId);
        log.info(" Generating C7 Final document for respondent ");
        Optional<Element<PartyDetails>> currentRespondent
            = caseData.getRespondents()
            .stream()
            .filter(
                respondent -> YesOrNo.Yes.equals(
                    respondent.getValue().getCurrentRespondent()))
            .findFirst();
        if (currentRespondent.isPresent()
            && Yes != currentRespondent.get().getValue().getResponse().getC7ResponseSubmitted()) {
            Element<PartyDetails> respondent = currentRespondent.get();
            respondent.getValue().setResponse(currentRespondent.get()
                                                  .getValue().getResponse().toBuilder().c7ResponseSubmitted(Yes).build());

            List<Element<PartyDetails>> respondents = new ArrayList<>(caseData.getRespondents());
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getId(),
                    respondent.getId()
                ))
                .findFirst()
                .ifPresent(party ->
                               respondents.set(
                                   respondents.indexOf(party),
                                   element(party.getId(), respondent.getValue())
                               )
                );
            caseData = caseData.toBuilder().respondents(respondents).build();
        }

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseDetails caseDetailsReturn = null;

        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C7_FINAL_ENGLISH,
            false
        );

        updateCurrentRespondent(caseData, null, partyId);
        if (document != null) {
            String partyName = caseData.getRespondents()
                .stream()
                .filter(element -> element.getId()
                    .toString()
                    .equalsIgnoreCase(partyId))
                .map(Element::getValue)
                .findFirst()
                .map(PartyDetails::getLabelForDynamicList)
                .orElse("");

            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            caseData = generateOtherC1aAndC8Documents(
                authorisation,
                caseData,
                currentRespondent,
                callbackRequest,
                document,
                partyName,
                userDetails
            );
            log.info("C1A and C8 Final document generated successfully for respondent ");

            caseDetailsReturn = caseService.updateCase(
                caseData,
                authorisation,
                    caseId,
                REVIEW_AND_SUBMIT
            );
        }

        if (caseDetailsReturn != null) {
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

