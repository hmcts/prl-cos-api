package uk.gov.hmcts.reform.prl.services.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION_ERROR;



@Slf4j
@Service
public class ChildrenAndOtherPeopleInThisApplicationChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> childrenWrapped = ofNullable(caseData.getChildAndOtherPeopleRelations());

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<ChildrenAndOtherPeopleRelation> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
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
        return false;
    }

    private boolean validateMandatoryFieldsCompleted(ChildrenAndOtherPeopleRelation child) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(child.getOtherPeopleFullName()));
        fields.add(ofNullable(child.getChildFullName()));
        fields.add(ofNullable(child.getChildLivesWith()));
        fields.add(ofNullable(child.getIsChildLivesWithPersonConfidential()));
        if (ofNullable(child.getChildAndOtherPeopleRelation()).get().equals(RelationshipsEnum.other)) {
            fields.add(ofNullable(child.getChildAndOtherPeopleRelationOtherDetails()));
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }


    @Override
    public boolean isStarted(CaseData caseData) {
        return ofNullable(caseData.getChildAndApplicantRelations()).isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState() {
        return TaskState.CANNOT_START_YET;
    }

}
