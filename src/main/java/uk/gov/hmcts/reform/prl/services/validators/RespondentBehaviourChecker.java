package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RESPONDENT_BEHAVIOUR_ERROR;


@Service
public class RespondentBehaviourChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        RespondentBehaviour respondentBehaviourData = caseData.getRespondentBehaviourData();
        if (respondentBehaviourData == null) {
            taskErrorService.addEventError(RESPONDENT_BEHAVIOUR,
                                           RESPONDENT_BEHAVIOUR_ERROR,
                                           RESPONDENT_BEHAVIOUR_ERROR.getError());
            return false;
        }

        Optional<String>  otherReason = ofNullable(respondentBehaviourData.getOtherReasonApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());
        if ((otherReason.isPresent() && !otherReason.get().isBlank()) && applicantStopRespondentList.isPresent()) {
            taskErrorService.removeError(RESPONDENT_BEHAVIOUR_ERROR);
            return true;
        } else {
            taskErrorService.addEventError(RESPONDENT_BEHAVIOUR,
                                           RESPONDENT_BEHAVIOUR_ERROR,
                                           RESPONDENT_BEHAVIOUR_ERROR.getError());

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
        boolean otherReasonCompleted = (otherReason.isPresent() && !(otherReason.get().isBlank()));
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());
        Optional<List<ApplicantStopFromRespondentDoingToChildEnum>> applicantStopFromRespondentDoingToChildList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoingToChild());
        boolean anyStarted = false;

        if (otherReasonCompleted || applicantStopRespondentList.isPresent() || applicantStopFromRespondentDoingToChildList.isPresent()) {
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
        boolean otherReasonCompleted = (otherReason.isPresent() && !(otherReason.get().isBlank()));
        Optional<List<ApplicantStopFromRespondentDoingEnum>> applicantStopRespondentList
            = ofNullable(respondentBehaviourData.getApplicantWantToStopFromRespondentDoing());
        return otherReasonCompleted
            && applicantStopRespondentList.isPresent() && !applicantStopRespondentList.isEmpty();
    }

}
