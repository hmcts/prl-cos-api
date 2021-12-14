package uk.gov.hmcts.reform.prl.services.validators;


import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.OTHER;

@Service
public class ChildChecker implements EventChecker{

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        if (childrenWrapped.isPresent() && childrenWrapped.get().size() != 0) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (Child c : children) {
                if(!(validateMandatoryFieldsCompleted(c))) {
                    taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
                    return false;
                }
            }
        }
        else {
            taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
            return false;
        }
        taskErrorService.removeError(CHILD_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        boolean anyStarted = false;

        if (childrenWrapped.isPresent() && childrenWrapped.get().size() != 0) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (Child c : children) {
                if (validateAnyFieldStarted(c)) {
                    anyStarted = true;
                }
            }
        }
        return anyStarted;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    private boolean validateMandatoryFieldsCompleted(Child child) {

        Optional<String> firstName = ofNullable(child.getFirstName());
        Optional<String> lastName = ofNullable(child.getLastName());
        Optional<LocalDate> dateOfBirth = ofNullable(child.getDateOfBirth());
        Optional<Gender> gender = ofNullable(child.getGender());
        Optional<String> otherGender = ofNullable(child.getOtherGender());
        Optional<List<OrderTypeEnum>> orderAppliedFor = ofNullable(child.getOrderAppliedFor());
        Optional<RelationshipsEnum> applicantsRelationshipToChild = ofNullable(child.getApplicantsRelationshipToChild());
        Optional<RelationshipsEnum> respondentsRelationshipToChild = ofNullable(child.getRespondentsRelationshipToChild());
        Optional<List<LiveWithEnum>> childLiveWith = ofNullable(child.getChildLiveWith());


        List<Optional> fields = new ArrayList<>();
        fields.add(firstName);
        fields.add(lastName);
        fields.add(dateOfBirth);
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(OTHER)) {
            fields.add(otherGender);
        }
        fields.add(orderAppliedFor);
        fields.add(applicantsRelationshipToChild);
        fields.add(respondentsRelationshipToChild);
        fields.add(childLiveWith);

        boolean emptyFieldPresent = fields.stream().anyMatch(Optional::isEmpty);

        return !emptyFieldPresent;
    }

    private boolean validateAnyFieldStarted(Child c) {
        Optional<String> firstName = ofNullable(c.getFirstName());
        Optional<String> lastName = ofNullable(c.getLastName());
        Optional<LocalDate> dateOfBirth = ofNullable(c.getDateOfBirth());
        Optional<Gender> gender = ofNullable(c.getGender());
        Optional<String> otherGender = ofNullable(c.getOtherGender());
        Optional<List<OrderTypeEnum>> orderAppliedFor = ofNullable(c.getOrderAppliedFor());
        Optional<RelationshipsEnum> applicantsRelationshipToChild = ofNullable(c.getApplicantsRelationshipToChild());
        Optional<RelationshipsEnum> respondentsRelationshipToChild = ofNullable(c.getRespondentsRelationshipToChild());
        Optional<List<LiveWithEnum>> childLiveWith = ofNullable(c.getChildLiveWith());
        Optional<YesNoDontKnow> childrenKnownToLocalAuthority = ofNullable(c.getChildrenKnownToLocalAuthority());
        Optional<YesNoDontKnow> childrenSubjectOfChildProtectionPlan = ofNullable(c.getChildrenSubjectOfChildProtectionPlan());

        List<Optional> fields = new ArrayList<>();
        fields.add(firstName);
        fields.add(lastName);
        fields.add(dateOfBirth);
        fields.add(gender);
        fields.add(otherGender);
        fields.add(orderAppliedFor);
        fields.add(applicantsRelationshipToChild);
        fields.add(respondentsRelationshipToChild);
        fields.add(childLiveWith);
        fields.add(childrenKnownToLocalAuthority);
        fields.add(childrenSubjectOfChildProtectionPlan);

        return  fields.stream().anyMatch(Optional::isPresent);
    }

}
