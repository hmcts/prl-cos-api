package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (Child c : children) {
                if (!(validateMandatoryFieldsCompleted(c)) || !(validateAdditionalFieldsCompleted(caseData))) {
                    taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
                    return false;
                }
            }
        }
        if (childrenWrapped.isEmpty()) {
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

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

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

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(child.getFirstName()));
        fields.add(ofNullable(child.getLastName()));
        fields.add(ofNullable(child.getDateOfBirth()));
        Optional<Gender> gender = ofNullable(child.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(other)) {
            fields.add(ofNullable(child.getOtherGender()));
        }
        fields.add(ofNullable(child.getOrderAppliedFor()));
        fields.add(ofNullable(child.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(child.getRespondentsRelationshipToChild()));
        Optional<List<LiveWithEnum>> childLivesWith = ofNullable(child.getChildLiveWith());
        if (childLivesWith.isPresent() && childLivesWith.get().equals(Collections.emptyList())) {
            return false;
        }
        if (childLivesWith.isPresent() && childLivesWith.get().contains(anotherPerson)) {
            Optional<List<Element<OtherPersonWhoLivesWithChild>>> personWhoLivesWithChildList =  ofNullable(child.getPersonWhoLivesWithChild());
            if (personWhoLivesWithChildList.isEmpty()
                || (personWhoLivesWithChildList.get().equals(Collections.emptyList()))) {
                return false;
            }
            personWhoLivesWithChildList.get().stream().map(Element::getValue).forEach(eachRow -> {
                fields.add(ofNullable(eachRow.getFirstName()));
                fields.add(ofNullable(eachRow.getLastName()));
                Optional<Address> address = ofNullable(eachRow.getAddress());
                fields.add(ofNullable(address));
                if (address.isPresent() && ofNullable(address.get().getAddressLine1()).isEmpty()) {
                    fields.add(Optional.empty());
                }
                fields.add(ofNullable(eachRow.getRelationshipToChildDetails()));
                fields.add(ofNullable(eachRow.getIsPersonIdentityConfidential()));
            });
        }
        fields.add(ofNullable(child.getParentalResponsibilityDetails()));
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private boolean validateAdditionalFieldsCompleted(CaseData caseData) {
        List<Optional<?>> fields = new ArrayList<>();

        Optional<YesNoDontKnow> childLocalAuth = ofNullable(caseData.getChildrenKnownToLocalAuthority());
        fields.add(childLocalAuth);
        if (childLocalAuth.isPresent() && childLocalAuth.get().equals(yes)) {
            fields.add(ofNullable(caseData.getChildrenKnownToLocalAuthorityTextArea()));
        }
        fields.add(ofNullable(caseData.getChildrenSubjectOfChildProtectionPlan()));

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    private boolean validateAnyFieldStarted(Child c) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(c.getFirstName()));
        fields.add(ofNullable(c.getLastName()));
        fields.add(ofNullable(c.getDateOfBirth()));
        fields.add(ofNullable(c.getGender()));
        fields.add(ofNullable(c.getOtherGender()));
        fields.add(ofNullable(c.getOrderAppliedFor()));
        fields.add(ofNullable(c.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(c.getRespondentsRelationshipToChild()));
        fields.add(ofNullable(c.getChildLiveWith()));

        fields.add(ofNullable(c.getParentalResponsibilityDetails()));

        return  fields.stream().anyMatch(Optional::isPresent);
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
