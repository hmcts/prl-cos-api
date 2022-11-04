package uk.gov.hmcts.reform.prl.services.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_NEW;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR_NEW;

@Service
public class AllegationsOfHarmCheckerNew implements EventChecker {


    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getAllegationOfHarmNewText() == null) {
            taskErrorService.addEventError(ALLEGATIONS_OF_HARM_NEW,
                ALLEGATIONS_OF_HARM_ERROR_NEW,
                ALLEGATIONS_OF_HARM_ERROR_NEW.getError());
            return false;
        }
        taskErrorService.removeError(ALLEGATIONS_OF_HARM_ERROR_NEW);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        return Boolean.FALSE;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }



}
