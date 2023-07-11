package uk.gov.hmcts.reform.prl.controllers.migration;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

@Tag(name = "migration-controller")
@Slf4j
@RestController
@RequestMapping("/migrate-data")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
public class MigrationController extends AbstractCallbackController {

    @Autowired
    @Qualifier("allTabsService")
    AllTabServiceImpl tabService;

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                @Parameter(hidden = true) String authorisation) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (State.JUDICIAL_REVIEW.equals(caseData.getState()) // && closedCaseIdFromProd.contains(caseData.getId())
        ) {
            caseData = caseData.toBuilder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();

            log.info("updating state via migration tool with  case id: {}",caseData.getId());
            log.info("updating tabs migration data for the case id: {}",caseData.getId());
            tabService.updateAllTabsIncludingConfTab(caseData);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated)
                    .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build();

        }

        log.info("updating tabs migration data for the case id: {}",caseData.getId());
        tabService.updateAllTabsIncludingConfTab(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .build();
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                @Parameter(hidden = true) String authorisation) {
        List<Long> closedCaseIdFromProd = List.of(Long.valueOf("1686576623239199"));
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        if (State.JUDICIAL_REVIEW.equals(caseData.getState()) // && closedCaseIdFromProd.contains(caseData.getId())
        ) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            log.info("updating state via migration tool with  case id: {}",caseData.getId());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated)
                            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build();

        }
        log.info("triggered migration about to submit event for the case id: {}",caseData.getId());

        return AboutToStartOrSubmitCallbackResponse.builder()
                .build();

    }
}
