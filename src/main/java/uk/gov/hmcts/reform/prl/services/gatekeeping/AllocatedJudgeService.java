package uk.gov.hmcts.reform.prl.services.gatekeeping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AllocatedJudgeService {

    public AllocatedJudge getAllocatedJudgeDetails(AllocatedJudge allocatedJudge, RefDataUserService refDataUserService) {
        return mapAllocatedJudge(allocatedJudge, refDataUserService);

    }

    private AllocatedJudge mapAllocatedJudge(AllocatedJudge allocatedJudge, RefDataUserService refDataUserService) {
        AllocatedJudge.AllocatedJudgeBuilder allocatedJudgeBuilder = AllocatedJudge.builder();
        if (null != allocatedJudge) {
            AllocatedJudgeTypeEnum allocatedJudgeType = allocatedJudge.getIsJudgeOrLegalAdviser();
            if (null != allocatedJudgeType && AllocatedJudgeTypeEnum.judge.equals(allocatedJudgeType)) {
                String[] personalCodes = new String[3];
                personalCodes[0] = allocatedJudge.getJudgeDetails().getPersonalCode();
                List<JudicialUsersApiResponse> judgeDetails =
                    refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                        .personalCode(personalCodes).build());
                if (null != judgeDetails && judgeDetails.size() > 0) {
                    allocatedJudgeBuilder.judgeName(judgeDetails.get(0).getSurname());
                    allocatedJudgeBuilder.judgeEmail(judgeDetails.get(0).getEmailId());
                    allocatedJudgeBuilder.judgeDetails(allocatedJudge.getJudgeDetails());
                }
            } else if (null != allocatedJudge.getLegalAdviserList() && null != allocatedJudge.getLegalAdviserList().getValue()) {
                allocatedJudgeBuilder.legalAdviserList(allocatedJudge.getLegalAdviserList());
            } else if (null != allocatedJudge.getTierOfJudiciary()) {
                allocatedJudgeBuilder.tierOfJudiciary(allocatedJudge.getTierOfJudiciary());
            }
            allocatedJudgeBuilder.isJudgeOrLegalAdviser(allocatedJudge.getIsJudgeOrLegalAdviser());
            allocatedJudgeBuilder.isSpecificJudgeOrLegalAdviserNeeded(allocatedJudge.getIsSpecificJudgeOrLegalAdviserNeeded());
        }
        return allocatedJudgeBuilder.build();
    }
}