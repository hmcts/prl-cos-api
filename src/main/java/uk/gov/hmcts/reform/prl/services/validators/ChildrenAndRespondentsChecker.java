package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_RESPONDENTS_ERROR;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S6813"})
public class ChildrenAndRespondentsChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Autowired
    @Lazy
    private EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getRelations().getChildAndRespondentRelations() != null) {

            if (caseData.getRelations().getChildAndRespondentRelations().stream().anyMatch(eachRelation ->
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
        taskErrorService.addEventError(
                CHILDREN_AND_RESPONDENTS,
                CHILDREN_AND_RESPONDENTS_ERROR,
                CHILDREN_AND_RESPONDENTS_ERROR.getError());
        return false;

    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getRelations().getChildAndRespondentRelations() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        if ((eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData) || eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData))
                && (eventsChecker.hasMandatoryCompleted(RESPONDENT_DETAILS, caseData) || eventsChecker.isFinished(RESPONDENT_DETAILS, caseData))) {
            return TaskState.NOT_STARTED;
        }
        return TaskState.CANNOT_START_YET;
    }

}
