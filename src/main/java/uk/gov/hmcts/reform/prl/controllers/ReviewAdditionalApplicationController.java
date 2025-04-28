package uk.gov.hmcts.reform.prl.controllers;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Slf4j
@SuppressWarnings({"squid:S5665"})
@RestController
@RequiredArgsConstructor
public class ReviewAdditionalApplicationController {

    private final DraftAnOrderService draftAnOrderService;
    private final AuthorisationService authorisationService;

    public static final String CONFIRMATION_HEADER = "# Order approved";


    @PostMapping(path = "/review-additional-application/mid-event", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Callback to review additional application")
    public AboutToStartOrSubmitCallbackResponse prepareReviewAdditionalApplicationCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            String language = CaseUtils.getLanguage(clientContext);
            Map<String, Object> caseDataUpdated = draftAnOrderService.getEligibleServeOrderDetails(
                authorisation,
                callbackRequest,
                language
            );
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
