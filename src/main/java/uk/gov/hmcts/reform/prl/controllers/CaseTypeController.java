package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.CaseViewField;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.definition.FieldTypeDefinition;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ExtendedCaseDetailsService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXTENDED_CASE_DETAILS_TITLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.enums.State.SUBMITTED_PAID;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/case-type")
public class CaseTypeController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final ExtendedCaseDetailsService extendedCaseDetailsService;
    private final CaseEventService caseEventService;
    private final CaseSummaryTabService caseSummaryTab;

    protected CaseTypeController(ObjectMapper objectMapper, EventService eventPublisher,
                                 AuthorisationService authorisationService,
                                 ExtendedCaseDetailsService extendedCaseDetailsService,
                                 CaseEventService caseEventService,
                                 CaseSummaryTabService caseSummaryTab) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.extendedCaseDetailsService = extendedCaseDetailsService;
        this.caseEventService = caseEventService;
        this.caseSummaryTab = caseSummaryTab;
    }

    @PostMapping(path = "/populate-extended-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate extended case details ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case details extended"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})

    public GetCaseCallbackResponse populateExtendedCaseDetails(
            @RequestHeader("Authorization") @Parameter(hidden = true) String authorization,
            @RequestHeader("ServiceAuthorization") @Parameter(hidden = true)
            String serviceAuthorization,
            @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorization, serviceAuthorization)) {
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            return getMetaData(caseData);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/fl401-add-case-numberWithPrefix", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback for adding case number and Prefix")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse addCaseNumberWithPrefixSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(callbackRequest.getCaseDetails().getId()));

            Optional<String> previousStateOpt = eventsForCase.stream()
                .map(CaseEventDetail::getStateId)
                .findFirst();

            if (previousStateOpt.isPresent()) {
                String previousState = previousStateOpt.get();
                if (AWAITING_INFORMATION.getValue().equalsIgnoreCase(previousState)) {
                    // extendedCaseDetailsService.setExtendedDetails(caseData, caseDataUpdated);
                    caseData = caseData.toBuilder().state(CASE_ISSUED).build();
                    caseDataUpdated.putAll(caseSummaryTab.updateTab(caseData));
                    caseDataUpdated.put(STATE_FIELD, CASE_ISSUED.getValue());
                } else if (SUBMITTED_PAID.getValue().equalsIgnoreCase(previousState)) {
                    // extendedCaseDetailsService.setExtendedDetails(caseData, caseDataUpdated);
                }
            }

            caseDataUpdated.put(ISSUE_DATE_FIELD, LocalDate.now());
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    public GetCaseCallbackResponse getMetaData(CaseData caseData) {
        String extendedCaseDetails = extendedCaseDetailsService.getExtendedDetails(caseData);
        log.info("extendedCaseDetails:{}", extendedCaseDetails);
        CaseViewField caseViewField = new CaseViewField();
        FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
        caseViewField.setId(EXTENDED_CASE_DETAILS_TITLE);
        caseViewField.setFormattedValue(extendedCaseDetails);
        caseViewField.setValue(extendedCaseDetails);
        caseViewField.setMetadata(true);
        fieldTypeDefinition.setId("Text");
        fieldTypeDefinition.setType("Text");
        caseViewField.setFieldTypeDefinition(fieldTypeDefinition);
        List<CaseViewField> caseViewFields = new ArrayList<>();
        caseViewFields.add(caseViewField);
        GetCaseCallbackResponse getCaseCallbackResponse
            = new GetCaseCallbackResponse();
        getCaseCallbackResponse.setMetadataFields(caseViewFields);
        return getCaseCallbackResponse;
    }
}
