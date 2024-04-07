package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MiamPolicyUpgradeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = false;

        Optional<YesOrNo> childInvolvedInMiam = ofNullable(caseData.getMiamPolicyUpgradeDetails().getChildInvolvedInMiam());

        if (childInvolvedInMiam.isPresent() && Yes.equals(childInvolvedInMiam.get())) {
            finished = true;
        }
        if (finished) {
            taskErrorService.removeError(MIAM_ERROR);
            return true;
        }
        Optional<YesOrNo> hasConsentOrder = ofNullable(caseData.getConsentOrder());
        taskErrorService.addEventError(MIAM, MIAM_ERROR, MIAM_ERROR.getError());
        if (hasConsentOrder.isPresent() && YesOrNo.Yes.equals(hasConsentOrder.get())) {
            taskErrorService.removeError(MIAM_ERROR);
        }
        return false;
    }


    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getMiamPolicyUpgradeDetails().getChildInvolvedInMiam()
        );
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
