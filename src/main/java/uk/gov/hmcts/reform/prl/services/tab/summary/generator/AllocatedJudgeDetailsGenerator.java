package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

@Component
public class AllocatedJudgeDetailsGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge = caseData.getAllocatedJudge();
        if (null != allocatedJudge) {
            String[] judgeOrLegalAdvisorDetails = splitLastNameAndEmailAddress(caseData.getAllocatedJudge());
            boolean isLastNameAndEmailAvailable = isLastNameAndEmailAvailable(judgeOrLegalAdvisorDetails);
            return CaseSummary.builder().allocatedJudgeDetails(
                AllocatedJudge.builder().courtName(CommonUtils.getValue(caseData.getCourtName()))
                    .emailAddress((isLastNameAndEmailAvailable) ? judgeOrLegalAdvisorDetails[1] : "").judgeTitle(" ")
                    .lastName((isLastNameAndEmailAvailable) ? judgeOrLegalAdvisorDetails[0] : "")
                    .tierOfJudiciaryType(getTierOfJudiciary(allocatedJudge)).build()).build();
        }

        return CaseSummary.builder().allocatedJudgeDetails(
            AllocatedJudge.builder().courtName(CommonUtils.getValue(caseData.getCourtName()))
                .emailAddress(" ").judgeTitle(" ").lastName(" ").build()).build();
    }

    private String getTierOfJudiciary(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge) {
        return (null != allocatedJudge.getTierOfJudiciary()  ? allocatedJudge.getTierOfJudiciary().getDisplayedValue() : "");
    }

    private boolean isLastNameAndEmailAvailable(String[] judgeOrLegalAdvisorDetails) {
        return (null != judgeOrLegalAdvisorDetails && judgeOrLegalAdvisorDetails.length == 2);
    }

    private String[] splitLastNameAndEmailAddress(uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge allocatedJudge) {
        if (null != allocatedJudge) {
            if (YesOrNo.Yes.equals(allocatedJudge.getIsSpecificJudgeOrLegalAdviserNeeded()) && null != allocatedJudge.getIsJudgeOrLegalAdviser()) {
                if (AllocatedJudgeTypeEnum.JUDGE.equals(allocatedJudge.getIsJudgeOrLegalAdviser())) {
                    String judgeNameAndEmail = allocatedJudge.getJudgeNameAndEmail();
                    if (null != judgeNameAndEmail) {

                        return judgeNameAndEmail.split("\\)")[0].split("\\(");
                    }
                } else if (AllocatedJudgeTypeEnum.LEGAL_ADVISER.equals(allocatedJudge.getIsJudgeOrLegalAdviser())) {
                    String legalAdviserNameAndEmail = allocatedJudge.getLegalAdviserDetails();
                    if (null != legalAdviserNameAndEmail) {
                        return legalAdviserNameAndEmail.split("\\)")[0].split("\\(");
                    }
                }
            }
        }
        return null;
    }
}


