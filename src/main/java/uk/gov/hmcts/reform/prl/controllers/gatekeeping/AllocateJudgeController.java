package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/allocateJudge")
public class AllocateJudgeController extends AbstractCallbackController {

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    private AllocatedJudgeService allocatedJudgeService;

    @PostMapping(path = "/pre-populate-legalAdvisor-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to retrieve legal advisor details")
    public AboutToStartOrSubmitCallbackResponse prePopulateLegalAdvisorDetails(
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("Prepopulate Legal Advisor - case id : {}", callbackRequest.getCaseDetails().getId());
        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("allocatedJudge", AllocatedJudge.builder().legalAdviserList(DynamicList.builder().value(DynamicListElement.EMPTY)
            .listItems(legalAdviserList).build()).build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();

    }

    @PostMapping(path = "/allocatedJudgeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "allocatedJudgeDetails. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Allocated Judge Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse allocateJudge(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        log.info("Allocate judge details for the case id : {}", caseData.getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("Allocate judge details for the case id before calling  : {}", caseData.getAllocatedJudge());
        AllocatedJudge allocatedJudge = allocatedJudgeService.getAllocatedJudgeDetails(caseData.getAllocatedJudge(), refDataUserService);
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        caseDataUpdated.put("allocatedJudge",allocatedJudge);
        caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}