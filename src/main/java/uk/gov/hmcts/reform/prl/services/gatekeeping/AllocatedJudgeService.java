package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HIGHCOURT_JUDGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAGISTRATES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TIER_OF_JUDICIARY;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AllocatedJudgeService {

    public AllocatedJudge getAllocatedJudgeDetails(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                                   RefDataUserService refDataUserService) {
        return mapAllocatedJudge(caseDataUpdated, legalAdviserList, refDataUserService);

    }

    private AllocatedJudge mapAllocatedJudge(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                             RefDataUserService refDataUserService) {
        AllocatedJudge.AllocatedJudgeBuilder allocatedJudgeBuilder = AllocatedJudge.builder();
        if (null != caseDataUpdated.get(TIER_OF_JUDICIARY)) {
            allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No);
            allocatedJudgeBuilder.tierOfJudiciary(getTierOfJudiciary(String.valueOf(caseDataUpdated.get(TIER_OF_JUDICIARY))));
        } else {
            if (null != caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR)) {
                if (AllocatedJudgeTypeEnum.judge.getId().equalsIgnoreCase(String.valueOf(caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR)))
                    && null != caseDataUpdated.get(JUDGE_NAME_EMAIL)) {
                    String[] judgePersonalCode = getPersonalCode(caseDataUpdated.get(JUDGE_NAME_EMAIL));
                    List<JudicialUsersApiResponse> judgeDetails =
                        refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                            .personalCode(getPersonalCode(caseDataUpdated.get(JUDGE_NAME_EMAIL))).build());
                    allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes);
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum.judge));
                    if (null != judgeDetails && judgeDetails.size() > 0) {
                        allocatedJudgeBuilder.judgeName(judgeDetails.get(0).getSurname());
                        allocatedJudgeBuilder.judgeEmail(judgeDetails.get(0).getEmailId());
                        allocatedJudgeBuilder.judgePersonalCode(judgePersonalCode[0]);
                    }
                } else if (null != legalAdviserList && null != legalAdviserList.getValue()) {
                    allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes);
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum.legalAdviser));
                    allocatedJudgeBuilder.legalAdviserList(legalAdviserList);
                }
            }
        }
        return allocatedJudgeBuilder.build();
    }

    public String[] getPersonalCode(Object judgeDetails) {
        String[] personalCodes = new String[3];
        try {
            personalCodes[0] = new ObjectMapper().readValue(new ObjectMapper()
                .writeValueAsString(judgeDetails), JudicialUser.class).getPersonalCode();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return personalCodes;
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
