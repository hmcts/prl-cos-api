package uk.gov.hmcts.reform.prl.controllers.barrister;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.ArrayList;
import java.util.List;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerAddService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REMOVE_BARRISTER_AND_PARTIES_LIST;

@Slf4j
@RestController
@RequestMapping("/barrister")
public class BarristerController extends AbstractCallbackController {
    private final AuthorisationService authorisationService;
    private final BarristerAddService barristerAddService;
    private final BarristerRemoveService barristerRemoveService;

    public BarristerController(ObjectMapper objectMapper, EventService eventPublisher,
                               BarristerAddService barristerAddService,
                               BarristerRemoveService barristerRemoveService,
                               AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.barristerAddService = barristerAddService;
        this.barristerRemoveService = barristerRemoveService;
        this.authorisationService = authorisationService;
    }

    @PostMapping(path = "/add/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to allocate a barrister on about-to-start")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside barrister/add/about-to-start for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            caseDataUpdated.put(ALLOCATED_BARRISTER, barristerAddService.getAllocatedBarrister(caseData, authorisation));

            AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                builder = AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated);
            return builder.build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/remove/about-to-start", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to remove a barrister on about-to-start")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse handleRemoveAboutToStart(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {

        log.info("Inside barrister/remove/about-to-start for case {}", callbackRequest.getCaseDetails().getId());
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<String> errorList = new ArrayList<>();
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

            DynamicList barristerList = barristerRemoveService.getBarristerListToRemove(caseData, authorisation);
            if (barristerList != null && !barristerList.getListItems().isEmpty()){
                caseDataUpdated.put(REMOVE_BARRISTER_AND_PARTIES_LIST, barristerList);
            } else {
                errorList.add("Parties are not represented by counsel at this stage");
            }

            AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder
                builder = AboutToStartOrSubmitCallbackResponse.builder().errors(errorList).data(caseDataUpdated);
            return builder.build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
