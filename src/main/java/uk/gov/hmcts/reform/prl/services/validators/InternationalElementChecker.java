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
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.INTERNATIONAL_ELEMENT_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class InternationalElementChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<YesOrNo> habitualResidence = ofNullable(caseData.getHabitualResidentInOtherState());
        Optional<String> habitualResidenceReason = ofNullable(caseData.getHabitualResidentInOtherStateGiveReason());

        Optional<YesOrNo> jurisdictionIssue = ofNullable(caseData.getJurisdictionIssue());
        Optional<String> jurisdictionIssueReason = ofNullable(caseData.getJurisdictionIssueGiveReason());

        Optional<YesOrNo> requestToForeignAuthority = ofNullable(caseData.getRequestToForeignAuthority());
        Optional<String> requestToForeignAuthorityReason = ofNullable(caseData.getRequestToForeignAuthorityGiveReason());

        if (habitualResidence.isEmpty() && jurisdictionIssue.isEmpty() && requestToForeignAuthority.isEmpty()) {
            return false;
        }

        boolean fieldsCompletedHR = true;
        boolean fieldsCompletedJI = true;
        boolean fieldsCompletedRfa = true;

        if (habitualResidence.isPresent() && habitualResidence.get().equals(Yes)) {
            fieldsCompletedHR = habitualResidenceReason.isPresent();
        }
        if (jurisdictionIssue.isPresent() && jurisdictionIssue.get().equals(Yes)) {
            fieldsCompletedJI = jurisdictionIssueReason.isPresent();
        }
        if (requestToForeignAuthority.isPresent() && requestToForeignAuthority.get().equals(Yes)) {
            fieldsCompletedRfa = requestToForeignAuthorityReason.isPresent();
        }

        if (fieldsCompletedHR && fieldsCompletedJI && fieldsCompletedRfa) {
            taskErrorService.removeError(INTERNATIONAL_ELEMENT_ERROR);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        boolean isStartedHR = false;
        boolean isStartedJI = false;
        boolean isStartedRfa = false;

        Optional<YesOrNo> habitualResidence = ofNullable(caseData.getHabitualResidentInOtherState());
        Optional<String> habitualResidenceReason = ofNullable(caseData.getHabitualResidentInOtherStateGiveReason());

        Optional<YesOrNo> jurisdictionIssue = ofNullable(caseData.getJurisdictionIssue());
        Optional<String> jurisdictionIssueReason = ofNullable(caseData.getJurisdictionIssueGiveReason());

        Optional<YesOrNo> requestToForeignAuthority = ofNullable(caseData.getRequestToForeignAuthority());
        Optional<String> requestToForeignAuthorityReason = ofNullable(caseData.getRequestToForeignAuthorityGiveReason());

        if (habitualResidence.isPresent() &&  (habitualResidenceReason.isEmpty() || habitualResidenceReason.get().isBlank())) {
            taskErrorService.addEventError(INTERNATIONAL_ELEMENT,
                                           INTERNATIONAL_ELEMENT_ERROR,
                                           INTERNATIONAL_ELEMENT_ERROR.getError());
            isStartedHR = true;
        }

        if (jurisdictionIssue.isPresent() &&  (jurisdictionIssueReason.isEmpty() || jurisdictionIssueReason.get().isBlank())) {
            taskErrorService.addEventError(INTERNATIONAL_ELEMENT,
                                           INTERNATIONAL_ELEMENT_ERROR,
                                           INTERNATIONAL_ELEMENT_ERROR.getError());
            isStartedJI = true;
        }

        if (requestToForeignAuthority.isPresent()
            &&  (requestToForeignAuthorityReason.isEmpty() || requestToForeignAuthorityReason.get().isBlank())) {
            taskErrorService.addEventError(INTERNATIONAL_ELEMENT,
                                           INTERNATIONAL_ELEMENT_ERROR,
                                           INTERNATIONAL_ELEMENT_ERROR.getError());
            isStartedRfa = true;
        }

        if (isStartedHR || isStartedJI || isStartedRfa) {
            taskErrorService.addEventError(
                INTERNATIONAL_ELEMENT,
                INTERNATIONAL_ELEMENT_ERROR,
                INTERNATIONAL_ELEMENT_ERROR.getError()
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
