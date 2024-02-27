package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR;



@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S6813"})
public class ChildrenAndOtherPeopleInThisApplicationChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Autowired
    @Lazy
    private EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> childrenWrapped = ofNullable(caseData
                .getRelations().getChildAndOtherPeopleRelations());

        if (childrenWrapped.isPresent() && !childrenWrapped.get().isEmpty()) {
            List<ChildrenAndOtherPeopleRelation> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();
            for (ChildrenAndOtherPeopleRelation c : children) {
                log.debug("ChildrenAndOtherPeopleInThisApplicationChecker - "
                              + "validateMandatoryFieldsCompleted :{} ",validateMandatoryFieldsCompleted(c));
                if (!(validateMandatoryFieldsCompleted(c))) {
                    taskErrorService.addEventError(
                        CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                        CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR,
                        CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR.getError());
                    return false;
                }
            }
            taskErrorService.removeError(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR);
            return true;
        }
        if (childrenWrapped.isEmpty() && isTaskCanBeEnabled(caseData)) {
            taskErrorService.addEventError(
                    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR,
                    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR.getError());
            return false;
        }
        return false;
    }

    private boolean validateMandatoryFieldsCompleted(ChildrenAndOtherPeopleRelation child) {

        final Optional<RelationshipsEnum> relationshipsEnum = ofNullable(child.getChildAndOtherPeopleRelation());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(child.getOtherPeopleFullName()));
        fields.add(ofNullable(child.getChildFullName()));
        fields.add(ofNullable(child.getChildLivesWith()));
        if (YesOrNo.Yes.equals(child.getChildLivesWith())) {
            fields.add(ofNullable(child.getIsChildLivesWithPersonConfidential()));
        }
        if (!relationshipsEnum.isEmpty()
            && relationshipsEnum.get().equals(RelationshipsEnum.other)) {
            fields.add(ofNullable(child.getChildAndOtherPeopleRelationOtherDetails()));
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }


    @Override
    public boolean isStarted(CaseData caseData) {
        return ofNullable(caseData.getRelations().getChildAndOtherPeopleRelations()).isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        if (isTaskCanBeEnabled(caseData)) {
            return TaskState.NOT_STARTED;
        }
        return TaskState.CANNOT_START_YET;
    }

    private boolean isTaskCanBeEnabled(CaseData caseData) {
        return (eventsChecker.hasMandatoryCompleted(CHILD_DETAILS_REVISED, caseData) || eventsChecker.isFinished(CHILD_DETAILS_REVISED, caseData))
                && (eventsChecker.hasMandatoryCompleted(OTHER_PEOPLE_IN_THE_CASE_REVISED, caseData)
                || eventsChecker.isFinished(OTHER_PEOPLE_IN_THE_CASE_REVISED, caseData));
    }

}
