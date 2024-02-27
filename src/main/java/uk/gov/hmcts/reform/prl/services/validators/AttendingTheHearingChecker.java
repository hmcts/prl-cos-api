package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttendingTheHearingChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        boolean finished;
        finished = allNonEmpty(
                    caseData.getAttendHearing().getIsWelshNeeded(),
                    caseData.getAttendHearing().getIsInterpreterNeeded(),
                    caseData.getAttendHearing().getIsDisabilityPresent(),
                    caseData.getAttendHearing().getIsSpecialArrangementsRequired(),
                    caseData.getAttendHearing().getIsIntermediaryNeeded()
        );

        if (finished) {
            taskErrorService.removeError(ATTENDING_THE_HEARING_ERROR);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        boolean isStarted = false;

        isStarted =  anyNonEmpty(
                caseData.getAttendHearing().getIsWelshNeeded(),
                caseData.getAttendHearing().getIsInterpreterNeeded(),
                caseData.getAttendHearing().getIsDisabilityPresent(),
                caseData.getAttendHearing().getSpecialArrangementsRequired(),
                caseData.getAttendHearing().getIsIntermediaryNeeded()
            );

        if (isStarted) {
            taskErrorService.addEventError(ATTENDING_THE_HEARING, ATTENDING_THE_HEARING_ERROR,
                                           ATTENDING_THE_HEARING_ERROR.getError());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
