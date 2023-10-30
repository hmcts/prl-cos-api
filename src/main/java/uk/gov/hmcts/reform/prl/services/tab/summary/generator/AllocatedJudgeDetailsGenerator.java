package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_ARRAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;

@Component
@Slf4j
public class AllocatedJudgeDetailsGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge = caseData.getAllocatedJudge();
        if (null != allocatedJudge) {
            String[] judgeOrLegalAdvisorDetails = splitLastNameAndEmailAddress(caseData.getAllocatedJudge());
            boolean isLastNameAndEmailAvailable = isLastNameAndEmailAvailable(judgeOrLegalAdvisorDetails);
            return CaseSummary.builder().allocatedJudgeDetails(
                AllocatedJudge.builder().courtName(CommonUtils.getValue(caseData.getCourtName()))
                    .emailAddress((isLastNameAndEmailAvailable) ? judgeOrLegalAdvisorDetails[1] : EMPTY_SPACE_STRING)
                    .lastName((isLastNameAndEmailAvailable) ? judgeOrLegalAdvisorDetails[0] : EMPTY_SPACE_STRING)
                    .tierOfJudiciaryType(getTierOfJudiciary(allocatedJudge))
                    .judgePersonalCode(allocatedJudge.getJudgePersonalCode()).isJudgeOrLegalAdviser(allocatedJudge.getIsJudgeOrLegalAdviser())
                    .isSpecificJudgeOrLegalAdviserNeeded(allocatedJudge.getIsSpecificJudgeOrLegalAdviserNeeded())
                    .tierOfJudge(null != allocatedJudge.getTierOfJudge() ? allocatedJudge.getTierOfJudge() : EMPTY_STRING)
                    .build()).build();
        }

        return CaseSummary.builder().allocatedJudgeDetails(
            AllocatedJudge.builder().courtName(CommonUtils.getValue(caseData.getCourtName()))
                .emailAddress(EMPTY_SPACE_STRING).tierOfJudiciaryType(EMPTY_STRING).lastName(EMPTY_SPACE_STRING)
                .tierOfJudge(EMPTY_STRING).build()).build();
    }

    private String getTierOfJudiciary(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge) {
        return (null != allocatedJudge.getTierOfJudiciary()  ? allocatedJudge.getTierOfJudiciary().getDisplayedValue() : EMPTY_STRING);
    }

    private boolean isLastNameAndEmailAvailable(String[] judgeOrLegalAdvisorDetails) {
        return (null != judgeOrLegalAdvisorDetails && judgeOrLegalAdvisorDetails.length == 2);
    }

    private String[] splitLastNameAndEmailAddress(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge) {

        if (null != allocatedJudge && YesOrNo.Yes.equals(allocatedJudge.getIsSpecificJudgeOrLegalAdviserNeeded())
            && null != allocatedJudge.getIsJudgeOrLegalAdviser()) {
            if (AllocatedJudgeTypeEnum.judge.equals(allocatedJudge.getIsJudgeOrLegalAdviser())) {
                String[] judgeOrLegalAdvisorDetails = new String[2];
                judgeOrLegalAdvisorDetails[0] = allocatedJudge.getJudgeName();
                judgeOrLegalAdvisorDetails[1] = allocatedJudge.getJudgeEmail();
                return judgeOrLegalAdvisorDetails;
            } else if (AllocatedJudgeTypeEnum.legalAdviser.equals(allocatedJudge.getIsJudgeOrLegalAdviser())) {
                String legalAdviserNameAndEmail = allocatedJudge.getLegalAdviserList().getValueLabel();
                if (null != legalAdviserNameAndEmail) {
                    return legalAdviserNameAndEmail.split("\\)")[0].split("\\(");
                }
            }

        }

        return EMPTY_ARRAY;
    }
}
