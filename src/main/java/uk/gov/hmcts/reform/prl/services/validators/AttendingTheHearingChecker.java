package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class AttendingTheHearingChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;


    @Override
    public boolean isFinished(CaseData caseData) {
        boolean finished;
        finished = allNonEmpty(
                    caseData.getIsWelshNeeded(),
                    caseData.getIsInterpreterNeeded(),
                    caseData.getIsDisabilityPresent(),
                    caseData.getIsSpecialArrangementsRequired(),
                    caseData.getIsIntermediaryNeeded()
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
                caseData.getIsWelshNeeded(),
                caseData.getIsInterpreterNeeded(),
                caseData.getIsDisabilityPresent(),
                caseData.getSpecialArrangementsRequired(),
                caseData.getIsIntermediaryNeeded()
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
}
