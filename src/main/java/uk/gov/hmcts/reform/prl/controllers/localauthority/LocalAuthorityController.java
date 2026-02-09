package uk.gov.hmcts.reform.prl.controllers.localauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.*;

@Slf4j
@RestController
@RequestMapping("/localauthority")
public class LocalAuthorityController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;

    public LocalAuthorityController(ObjectMapper objectMapper, EventService eventPublisher,
                                    AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/add/aboutToSubmit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add a Local authority on about to submit")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAddAboutToSubmit(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside Local authority/add/submitted for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );

            OrganisationPolicy localAuthorityOrganisationPolicy = caseData.getLocalAuthoritySolicitorOrganisationPolicy();
            caseDataUpdated.put(
                LOCAL_AUTHORITY_SOLICITOR_ORGANISATION_POLICY,
                localAuthorityOrganisationPolicy.toBuilder().orgPolicyCaseAssignedRole("[LASOLICITOR]").build()
            );
            caseDataUpdated.put(
                LOCAL_AUTHORITY_SOCIAL_WORKER_ORGANISATION_POLICY,
                localAuthorityOrganisationPolicy.toBuilder().orgPolicyCaseAssignedRole("[LASOCIALWORKER]").build()
            );
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
