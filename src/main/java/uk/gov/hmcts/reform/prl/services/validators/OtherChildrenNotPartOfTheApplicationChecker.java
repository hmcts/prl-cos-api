package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR;

@Service
public class OtherChildrenNotPartOfTheApplicationChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        taskErrorService.addEventError(
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR.getError()
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
        return TaskState.NOT_STARTED;
    }

}
