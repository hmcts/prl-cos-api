package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_APPLICANTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_APPLICANTS_ERROR;

@Service
public class ChildrenAndApplicantsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Autowired
    @Lazy
    EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getChildAndApplicantRelations() != null) {

            if (caseData.getChildAndApplicantRelations().stream().anyMatch(eachRelation ->
                    RelationshipsEnum.other.getDisplayedValue().equalsIgnoreCase(eachRelation.getValue()
                            .getChildAndApplicantRelation().getDisplayedValue())
                            && eachRelation.getValue().getChildAndApplicantRelationOtherDetails() == null)) {
                taskErrorService.addEventError(
                        CHILDREN_AND_APPLICANTS,
                        CHILDREN_AND_APPLICANTS_ERROR,
                        CHILDREN_AND_APPLICANTS_ERROR.getError());

                return false;
            }
            taskErrorService.removeError(
                    CHILDREN_AND_APPLICANTS_ERROR);

            return true;
        }

        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getChildAndApplicantRelations() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {

        if (eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData) && eventsChecker.isFinished(APPLICANT_DETAILS, caseData)) {
            return TaskState.NOT_STARTED;
        }
        return TaskState.CANNOT_START_YET;
    }

}
