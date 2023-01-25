package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
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
    CaseSummaryTabService caseSummaryTabService;

    @PostMapping(path = "/pre-populate-legalAdvisor-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to retrieve legal advisor details")
    public AboutToStartOrSubmitCallbackResponse prePopulateLegalAdvisorDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("*** request recieved to get the legalAdvisor details : {}", callbackRequest.toString());

        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(DynamicListElement.builder().code("test1").label("test1").build());
        dynamicListElements.add(DynamicListElement.builder().code("test2").label("test2").build());
        dynamicListElements.add(DynamicListElement.builder().code("test3").label("test3").build());

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        caseDataUpdated.put("legalAdvisorList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(dynamicListElements)
            .build());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();

    }

    @PostMapping(path = "/allocatedJudgeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "allocatedJudgeDetails. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Allocated Judge Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse allocateJudge(@RequestHeader("Authorization") @Parameter(hidden = true) String authorization,
                                                             @RequestHeader("ServiceAuthorization") @Parameter(hidden = true)
                                                             String serviceAuthorization,
                                                             @RequestBody CallbackRequest callbackRequest) {
        log.info("*** request recieved to get the allocate Judge details : {}", callbackRequest.toString());
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("*** allocate judge details for the case id : {}", caseData.getId());
        AllocatedJudge allocatedJudge = mapAllocatedJudge(caseDataUpdated);
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        caseDataUpdated.put("allocatedJudge",allocatedJudge);
        caseSummaryTabService.updateTab(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private AllocatedJudge mapAllocatedJudge(Map<String, Object> caseDataUpdated) {
        AllocatedJudge.AllocatedJudgeBuilder allocatedJudgeBuilder = AllocatedJudge.builder();
        if (null != caseDataUpdated.get("isSpecificJudgeOrLegalAdviserNeeded")) {
            YesOrNo isSpecificJudgeOrLegalAdviserNeeded = (YesOrNo) caseDataUpdated.get("isSpecificJudgeOrLegalAdviserNeeded");
            if (YesOrNo.Yes.equals(isSpecificJudgeOrLegalAdviserNeeded)) {
                allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded((YesOrNo) caseDataUpdated.get("isSpecificJudgeOrLegalAdviserNeeded"));
                if (null != caseDataUpdated.get("isJudgeOrLegalAdviser")) {
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum) caseDataUpdated.get("isJudgeOrLegalAdviser"));
                    if (null != caseDataUpdated.get("judge")) {
                        allocatedJudgeBuilder.judgeNameAndEmail((String) caseDataUpdated.get("judge"));
                    }
                    if (null != caseDataUpdated.get("legalAdvisorList")) {
                        allocatedJudgeBuilder.legalAdviserDetails(((DynamicList) caseDataUpdated.get("legalAdvisorList")).getValueLabel());
                    }
                }
            } else {
                if (null != caseDataUpdated.get("tierOfJudiciary")) {
                    allocatedJudgeBuilder.tierOfJudiciary((TierOfJudiciaryEnum) caseDataUpdated.get("tierOfJudiciary"));
                }
            }
        }
        return allocatedJudgeBuilder.build();
    }

}
