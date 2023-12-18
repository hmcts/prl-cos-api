package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;

@Service
public class UploadDocumentChecker implements EventChecker {

    //always returns false as not part of task list validation or labelling
    @Override
    public boolean isFinished(CaseData caseData) {
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
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
