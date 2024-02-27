package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationTypeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getOrdersApplyingFor(),
            caseData.getTypeOfChildArrangementsOrder(),
            caseData.getNatureOfOrder(),
            caseData.getConsentOrder(),
            caseData.getDraftConsentOrderFile(),
            caseData.getApplicationPermissionRequired(),
            caseData.getApplicationPermissionRequiredReason(),
            caseData.getApplicationDetails()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = allNonEmpty(
            caseData.getOrdersApplyingFor(),
            caseData.getNatureOfOrder(),
            caseData.getConsentOrder(),
            caseData.getApplicationPermissionRequired(),
            caseData.getApplicationDetails()
        );

        if (finished) {
            taskErrorService.removeError(TYPE_OF_APPLICATION_ERROR);
            return true;
        }
        taskErrorService.addEventError(TYPE_OF_APPLICATION,
                                       TYPE_OF_APPLICATION_ERROR,
                                       TYPE_OF_APPLICATION_ERROR.getError());
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
