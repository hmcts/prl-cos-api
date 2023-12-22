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
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.LITIGATION_CAPACITY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LitigationCapacityChecker implements EventChecker {
    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean litigationFactorsComplete = ofNullable(caseData.getLitigationCapacityFactors()).isPresent();
        boolean litigationReferralsComplete = ofNullable(caseData.getLitigationCapacityReferrals()).isPresent();
        boolean litigationOtherComplete = ofNullable(caseData.getLitigationCapacityOtherFactors()).isPresent();
        boolean litigationOtherDetailsComplete = ofNullable(caseData.getLitigationCapacityOtherFactorsDetails())
            .isPresent();

        Optional<YesOrNo> litigationOther = ofNullable(caseData.getLitigationCapacityOtherFactors());

        if ((litigationOtherComplete && litigationOtherDetailsComplete)
            || (litigationOtherComplete && (litigationOther.isPresent() && litigationOther.get().equals(No)))) {
            taskErrorService.removeError(LITIGATION_CAPACITY_ERROR);
            return true;
        }

        if (!litigationOtherComplete && (litigationFactorsComplete || litigationReferralsComplete)) {
            taskErrorService.removeError(LITIGATION_CAPACITY_ERROR);
            return true;
        }
        return false;

    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<YesOrNo> otherFactors = ofNullable(caseData.getLitigationCapacityOtherFactors());
        if (otherFactors.isPresent()
            && otherFactors.get().equals(Yes)
            && ofNullable(caseData.getLitigationCapacityOtherFactorsDetails()).isEmpty()) {
            taskErrorService.addEventError(LITIGATION_CAPACITY, LITIGATION_CAPACITY_ERROR,
                                           LITIGATION_CAPACITY_ERROR.getError()
            );
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
