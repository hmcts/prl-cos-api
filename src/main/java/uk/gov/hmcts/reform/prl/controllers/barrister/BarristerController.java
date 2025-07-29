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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAllocationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;

@Slf4j
@RestController
@RequestMapping("/barrister")
public class BarristerController extends AbstractCallbackController {
    private final BarristerAllocationService barristerAllocationService;
    private final UserService userService;

    public BarristerController(ObjectMapper objectMapper, EventService eventPublisher,
                               BarristerAllocationService barristerAllocationService, UserService userService) {
        super(objectMapper, eventPublisher);
        this.barristerAllocationService = barristerAllocationService;
        this.userService = userService;
    }

    @PostMapping(path = "/choose-barrister-to-add/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to allocate a barrister on about-to-start")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        UserDetails userDetails = userService.getUserDetails(authorisation);

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        caseDataUpdated.put(ALLOCATED_BARRISTER, barristerAllocationService.getAllocatedBarrister(caseData, userDetails, authorisation));

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
            builder = AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated);
        return builder.build();
    }
}
