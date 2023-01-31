package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.judicial.JudicialUserInfoService;
import uk.gov.hmcts.reform.prl.services.staff.StaffUserInfoService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HIGHCOURT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAGISTRATES;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/allocateJudge")
public class AllocateJudgeController extends AbstractCallbackController {

    @Autowired
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Autowired
    JudicialUserInfoService judicialUserInfoService;

    @Autowired
    StaffUserInfoService staffUserInfoService;

    @PostMapping(path = "/pre-populate-legalAdvisor-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to retrieve legal advisor details")
    public AboutToStartOrSubmitCallbackResponse prePopulateLegalAdvisorDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        log.info("*** request recieved to get the legalAdvisor details : {}");
        List<DynamicListElement> legalAdviserList = staffUserInfoService.getLegalAdvisorList(authorisation);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("legalAdviserList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
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
        log.info("*** allocate judge details for the case id : {}", caseData.getId());
        log.info("*** ********allocate judge details for the case id before mapping : {}", caseData.getAllocatedJudge());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        AllocatedJudge allocatedJudge = mapAllocatedJudge(authorization,serviceAuthorization,caseDataUpdated,caseData.getLegalAdviserList());
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        //caseDataUpdated.put("allocatedJudge",allocatedJudge);
        caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));
        log.info("*** ********allocate judge details after populating for the case id : {}", caseData.getAllocatedJudge());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private AllocatedJudge mapAllocatedJudge(String authorization, String serviceAuthorization, Map<String, Object> caseDataUpdated,
                                             DynamicList legalAdviserList) {
        AllocatedJudge.AllocatedJudgeBuilder allocatedJudgeBuilder = AllocatedJudge.builder();
        if (null != caseDataUpdated.get("tierOfJudiciary")) {
            allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No);
            allocatedJudgeBuilder.tierOfJudiciary(getTierOfJudiciary(String.valueOf(caseDataUpdated.get("tierOfJudiciary"))));
        } else {
            if (null != caseDataUpdated.get("isJudgeOrLegalAdviser")) {
                if (null != caseDataUpdated.get("judgeNameAndEmail")) {
                    String[] personalCodes = new String[3];
                    try {
                        personalCodes[0] = new ObjectMapper().readValue(new ObjectMapper()
                            .writeValueAsString(caseDataUpdated.get("judgeNameAndEmail")),JudicialUser.class).getPersonalCode();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    log.info("*** ********PersonalCode for the selected judge id : {}", null != personalCodes ? personalCodes.length : personalCodes);
                    JudicialUsersApiResponse judgeDetails = judicialUserInfoService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                        .personalCode(personalCodes).build(),serviceAuthorization,authorization);
                    allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes);
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum.JUDGE));
                    allocatedJudgeBuilder.judgeName(judgeDetails.getSurname());
                    allocatedJudgeBuilder.judgeEmail(judgeDetails.getEmailId());
                }
                if (null != legalAdviserList && null != legalAdviserList.getValue()) {
                    allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes);
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum.LEGAL_ADVISER));
                    allocatedJudgeBuilder.legalAdviserList(legalAdviserList);
                }
            }
        }
        return allocatedJudgeBuilder.build();
    }

    private TierOfJudiciaryEnum getTierOfJudiciary(String tierOfJudiciary) {
        TierOfJudiciaryEnum tierOfJudiciaryEnum = null;
        switch (tierOfJudiciary) {
            case DISTRICT_JUDGE:
                tierOfJudiciaryEnum = TierOfJudiciaryEnum.DISTRICT_JUDGE;
                break;
            case MAGISTRATES:
                tierOfJudiciaryEnum = TierOfJudiciaryEnum.MAGISTRATES;
                break;
            case CIRCUIT_JUDGE:
                tierOfJudiciaryEnum = TierOfJudiciaryEnum.CIRCUIT_JUDGE;
                break;
            case HIGHCOURT_JUDGE:
                tierOfJudiciaryEnum = TierOfJudiciaryEnum.HIGHCOURT_JUDGE;
                break;
            default:
                break;
        }
        return tierOfJudiciaryEnum;
    }
}
