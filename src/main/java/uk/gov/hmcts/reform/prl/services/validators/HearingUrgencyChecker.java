package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class HearingUrgencyChecker implements EventChecker{
    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getIsCaseUrgent() != null) {
            if (caseData.getIsCaseUrgent().equals(NO)) {
                return allNonEmpty(
                    caseData.getDoYouNeedAWithoutNoticeHearing(),
                    caseData.getDoYouRequireAHearingWithReducedNotice(),
                    caseData.getAreRespondentsAwareOfProceedings()
                );
            } else {
                return allNonEmpty(
                    caseData.getCaseUrgencyTimeAndReason(),
                    caseData.getEffortsMadeWithRespondents(),
                    caseData.getDoYouNeedAWithoutNoticeHearing(),
                    caseData.getReasonsForApplicationWithoutNotice(),
                    caseData.getDoYouRequireAHearingWithReducedNotice(),
                    caseData.getSetOutReasonsBelow(),
                    caseData.getAreRespondentsAwareOfProceedings()
                );
            }
        }
        return false;
    }
    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getIsCaseUrgent(),
            caseData.getCaseUrgencyTimeAndReason(),
            caseData.getEffortsMadeWithRespondents(),
            caseData.getDoYouNeedAWithoutNoticeHearing(),
            caseData.getReasonsForApplicationWithoutNotice(),
            caseData.getDoYouRequireAHearingWithReducedNotice(),
            caseData.getSetOutReasonsBelow(),
            caseData.getAreRespondentsAwareOfProceedings()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        if (caseData.getIsCaseUrgent() != null) {
            switch (caseData.getIsCaseUrgent()) {
                case YES:
                    return allNonEmpty(
                        caseData.getCaseUrgencyTimeAndReason(),
                        caseData.getEffortsMadeWithRespondents(),
                        caseData.getDoYouNeedAWithoutNoticeHearing(),
                        caseData.getReasonsForApplicationWithoutNotice(),
                        caseData.getDoYouRequireAHearingWithReducedNotice(),
                        caseData.getSetOutReasonsBelow()
                    );
                case NO:
                    return true;
            }
        }
        return false;
    }

}
