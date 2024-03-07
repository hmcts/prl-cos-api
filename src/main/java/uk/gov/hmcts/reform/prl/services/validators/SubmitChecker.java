package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class  SubmitChecker implements EventChecker {

    private final SubmitAndPayChecker submitAndPayChecker;

    @Override
    public boolean isFinished(CaseData caseData) {
        return submitAndPayChecker.isFinished(caseData);
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
