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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAGISTRATES;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AllocatedJudgeService {

    @Autowired
    private final RefDataUserService refDataUserService;


    public AllocatedJudge getAllocatedJudgeDetails(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList) {
        return mapAllocatedJudge(caseDataUpdated, legalAdviserList);

    }

    private AllocatedJudge mapAllocatedJudge(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList) {
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
                            .writeValueAsString(caseDataUpdated.get("judgeNameAndEmail")), JudicialUser.class).getPersonalCode();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    log.info("*** ********PersonalCode for the selected judge id : {}", null != personalCodes ? personalCodes.length : personalCodes);
                    List<JudicialUsersApiResponse> judgeDetails =
                        refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().personalCode(personalCodes).build());
                    allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes);
                    allocatedJudgeBuilder.isJudgeOrLegalAdviser((AllocatedJudgeTypeEnum.JUDGE));
                    if (null != judgeDetails && judgeDetails.size() > 0) {
                        allocatedJudgeBuilder.judgeName(judgeDetails.get(0).getSurname());
                        allocatedJudgeBuilder.judgeEmail(judgeDetails.get(0).getEmailId());
                    }
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
