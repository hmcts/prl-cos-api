package uk.gov.hmcts.reform.prl.controllers.localauthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.localauthority.LocalAuthoritySocialWorker;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.localauthority.SocialWorkerAddService;
import uk.gov.hmcts.reform.prl.services.localauthority.SocialWorkerRemoveService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER;

@Slf4j
@RestController
@RequestMapping("/localauthority/socialworker")
public class SocialWorkerController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final SocialWorkerAddService socialWorkerAddService;
    private final SocialWorkerRemoveService socialWorkerRemoveService;

    public SocialWorkerController(ObjectMapper objectMapper, EventService eventPublisher,
                                  SocialWorkerAddService socialWorkerAddService,
                                  SocialWorkerRemoveService socialWorkerRemoveService,
                                  AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.socialWorkerAddService = socialWorkerAddService;
        this.socialWorkerRemoveService = socialWorkerRemoveService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/add/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add a Local authority on submitted")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleAddSubmitted(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside Local authority/add/submitted for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            if (caseData.getLocalAuthoritySocialWorker() != null) {
                socialWorkerAddService.notifySocialWorker(caseData);
            }
        } else {
            throw new InvalidClientException(INVALID_CLIENT);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @PostMapping(path = "/remove/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to add a local authority on submitted")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleRemoveSubmitted(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside local authority/remove/submitted for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            if (caseData.getLocalAuthoritySocialWorker() != null) {
                socialWorkerRemoveService.notifySocialWorker(caseData);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
        } else {
            throw new InvalidClientException();
        }
    }
}
