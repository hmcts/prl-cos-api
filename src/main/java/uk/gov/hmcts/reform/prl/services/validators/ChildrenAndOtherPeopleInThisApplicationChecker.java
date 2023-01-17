package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR;

@Service
public class ChildrenAndOtherPeopleInThisApplicationChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        taskErrorService.addEventError(
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR.getError()
        );
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState() {
        return TaskState.CANNOT_START_YET;
    }

}
