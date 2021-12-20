package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.OTHER;

@Service
public class ChildChecker implements EventChecker {

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
                if (!(validateMandatoryFieldsCompleted(c))) {
                    taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
                    return false;
                }
            }
        } else {
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

        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(child.getFirstName()));
        fields.add(ofNullable(child.getLastName()));
        fields.add(ofNullable(child.getDateOfBirth()));
        Optional<Gender> gender = ofNullable(child.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(OTHER)) {
            fields.add(ofNullable(child.getOtherGender()));
        }
        fields.add(ofNullable(child.getOrderAppliedFor()));
        fields.add(ofNullable(child.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(child.getRespondentsRelationshipToChild()));
        fields.add(ofNullable(child.getChildLiveWith()));

        boolean emptyFieldPresent = fields.stream().anyMatch(Optional::isEmpty);

        return !emptyFieldPresent;
    }

    private boolean validateAnyFieldStarted(Child c) {

        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(c.getFirstName()));
        fields.add(ofNullable(c.getLastName()));
        fields.add(ofNullable(c.getDateOfBirth()));
        fields.add(ofNullable(c.getGender()));
        fields.add(ofNullable(c.getOtherGender()));
        fields.add(ofNullable(c.getOrderAppliedFor()));
        fields.add(ofNullable(c.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(c.getRespondentsRelationshipToChild()));
        fields.add(ofNullable(c.getChildLiveWith()));
        fields.add(ofNullable(c.getChildrenKnownToLocalAuthority()));
        fields.add(ofNullable(c.getChildrenSubjectOfChildProtectionPlan()));

        return  fields.stream().anyMatch(Optional::isPresent);
    }

}
