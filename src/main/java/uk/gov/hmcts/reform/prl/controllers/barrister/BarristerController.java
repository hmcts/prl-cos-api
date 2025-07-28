package uk.gov.hmcts.reform.prl.controllers.barrister;

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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAddService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequestMapping("/barrister")
public class BarristerController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final BarristerAddService barristerAddService;

    public BarristerController(ObjectMapper objectMapper, EventService eventPublisher,
                               BarristerAddService barristerAddService,
                               AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.barristerAddService = barristerAddService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/add/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to allocate a barrister on about-to-start")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            caseDataUpdated.put(ALLOCATED_BARRISTER, barristerAddService.getAllocatedBarrister(caseData));

            AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                builder = AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated);
            return builder.build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
