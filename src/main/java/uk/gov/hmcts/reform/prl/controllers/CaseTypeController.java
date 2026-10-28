package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.CaseViewField;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ExtendedCaseDetailsService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXTENDED_CASE_DETAILS_TITLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/case-type")
public class CaseTypeController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final ExtendedCaseDetailsService extendedCaseDetailsService;

    protected CaseTypeController(ObjectMapper objectMapper, EventService eventPublisher,
                                 AuthorisationService authorisationService,
                                 ExtendedCaseDetailsService extendedCaseDetailsService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.extendedCaseDetailsService = extendedCaseDetailsService;
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
            String extendedCaseDetails = extendedCaseDetailsService.getExtendedDetails(caseData);

            GetCaseCallbackResponse getCaseCallbackResponse = new GetCaseCallbackResponse();
            List<CaseViewField> metaDataFields = new ArrayList();
            CaseViewField caseViewField = new CaseViewField();
            caseViewField.setId(EXTENDED_CASE_DETAILS_TITLE);
            caseViewField.setFormattedValue(extendedCaseDetails);
            metaDataFields.add(caseViewField);
            getCaseCallbackResponse.setMetadataFields(metaDataFields);
            return getCaseCallbackResponse;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
