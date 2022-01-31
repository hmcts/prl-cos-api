package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;


@Service
public class RespondentBehaviourChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {
        boolean finished;
        RespondentBehaviour respondentBehaviourData = caseData.getRespondentBehaviourData();
        if (respondentBehaviourData == null) {
            return false;
        }
        Optional<String>  otherReason = ofNullable(respondentBehaviourData.getOtherReasonApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());

        if (otherReason.isPresent() && applicantStopRespondentList.get().size() != 0) {
            finished = otherReason.isPresent()
                && applicantStopRespondentList.get().size() != 0;
            if (finished) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        RespondentBehaviour respondentBehaviourData = caseData.getRespondentBehaviourData();
        if (respondentBehaviourData == null) {
            return false;
        }
        Optional<String>  otherReason = ofNullable(respondentBehaviourData.getOtherReasonApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingToChildEnum>> applicantStopFromRespondentDoingToChildList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoingToChild());
        boolean anyStarted = false;

        if (otherReason.isPresent() || applicantStopRespondentList.get().size() != 0
            || applicantStopFromRespondentDoingToChildList.get().size() != 0) {
            anyStarted = true;
        }

        return  anyStarted;
    }


    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        RespondentBehaviour respondentBehaviourData = caseData.getRespondentBehaviourData();
        if (respondentBehaviourData == null) {
            return false;
        }
        Optional<String>  otherReason = ofNullable(respondentBehaviourData.getOtherReasonApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());
        return otherReason.isPresent()
            && applicantStopRespondentList.get().size() != 0;
    }

}
