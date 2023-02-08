package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_RESPONDENTS_ERROR;

@Service
public class ChildrenAndRespondentsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getChildAndRespondentRelations() != null) {

            if (caseData.getChildAndRespondentRelations().stream().anyMatch(eachRelation ->
                    RelationshipsEnum.other.getDisplayedValue().equalsIgnoreCase(eachRelation.getValue()
                            .getChildAndRespondentRelation().getDisplayedValue())
                            && eachRelation.getValue().getChildAndRespondentRelationOtherDetails() == null)) {
                taskErrorService.addEventError(
                        CHILDREN_AND_RESPONDENTS,
                        CHILDREN_AND_RESPONDENTS_ERROR,
                        CHILDREN_AND_RESPONDENTS_ERROR.getError());

                return false;
            }
            taskErrorService.removeError(
                    CHILDREN_AND_RESPONDENTS_ERROR);

            return true;
        }

        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getChildAndRespondentRelations() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.CANNOT_START_YET;
    }

}
