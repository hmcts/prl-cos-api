package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OtherProceedingsService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;


@Slf4j
@RestController
@RequestMapping("/other-proceedings")
public class OtherProceedingsController extends AbstractCallbackController {

    private final AuthorisationService authorisationService;

    private final OtherProceedingsService otherProceedingsService;

    @Autowired
    public OtherProceedingsController(AuthorisationService authorisationService, OtherProceedingsService otherProceedingsService,
                                      ObjectMapper objectMapper, EventService eventPublisher) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.otherProceedingsService = otherProceedingsService;
    }

    @PostMapping(path = "/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to other proceedings about to submit")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse otherProceedingsAboutToSubmit(
            @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
            @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
            @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
