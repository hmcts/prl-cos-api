package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allEmpty;

@Service
public class InternationalElementChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {

        YesOrNo habitualResident = caseData.getHabitualResidentInOtherState();
        YesOrNo jurisdictionIssue = caseData.getJurisdictionIssue();
        YesOrNo requestToForeign = caseData.getRequestToForeignAuthority();

        if (allEmpty(habitualResident, jurisdictionIssue, requestToForeign)) {
            return false;
        }

        boolean fieldsCompleted = true;

        if (habitualResident != null && habitualResident.equals(YES)) {
            fieldsCompleted = caseData.getHabitualResidentInOtherStateGiveReason() != null;
        }
        if (jurisdictionIssue != null && jurisdictionIssue.equals(YES)) {
            fieldsCompleted = caseData.getJurisdictionIssueGiveReason() != null;
        }
        if (requestToForeign != null && requestToForeign.equals(YES)) {
            fieldsCompleted = caseData.getRequestToForeignAuthorityGiveReason() != null;
        }

        return fieldsCompleted;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getHabitualResidentInOtherState(),
            caseData.getJurisdictionIssue(),
            caseData.getRequestToForeignAuthority()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
