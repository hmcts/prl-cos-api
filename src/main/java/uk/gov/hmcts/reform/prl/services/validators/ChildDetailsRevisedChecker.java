package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_REVISED_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildDetailsRevisedChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<ChildDetailsRevised>>> childrenWrapped = ofNullable(caseData.getNewChildDetails());

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<ChildDetailsRevised> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (ChildDetailsRevised c : children) {
                log.debug("validateMandatoryFieldsCompleted  :{} ",validateMandatoryFieldsCompleted(c));
                log.debug("validateAdditionalFieldsCompleted  :{} ",validateAdditionalFieldsCompleted(caseData));
                if (!(validateMandatoryFieldsCompleted(c)) || !(validateAdditionalFieldsCompleted(caseData))) {
                    taskErrorService.addEventError(CHILD_DETAILS_REVISED, CHILD_DETAILS_REVISED_ERROR, CHILD_DETAILS_REVISED_ERROR.getError());
                    return false;
                }
            }
        }
        if (childrenWrapped.isEmpty()) {
            taskErrorService.addEventError(CHILD_DETAILS_REVISED, CHILD_DETAILS_REVISED_ERROR, CHILD_DETAILS_REVISED_ERROR.getError());
            return false;
        }
        taskErrorService.removeError(CHILD_DETAILS_REVISED_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<List<Element<ChildDetailsRevised>>> childrenWrapped = ofNullable(caseData.getNewChildDetails());

        boolean anyStarted = false;

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<ChildDetailsRevised> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (ChildDetailsRevised c : children) {
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

    private boolean validateMandatoryFieldsCompleted(ChildDetailsRevised child) {

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
        fields.add(ofNullable(child.getParentalResponsibilityDetails()));
        fields.add(ofNullable(child.getWhoDoesTheChildLiveWith()));
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

    private boolean validateAnyFieldStarted(ChildDetailsRevised c) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(c.getFirstName()));
        fields.add(ofNullable(c.getLastName()));
        fields.add(ofNullable(c.getDateOfBirth()));
        fields.add(ofNullable(c.getGender()));
        fields.add(ofNullable(c.getOtherGender()));
        fields.add(ofNullable(c.getOrderAppliedFor()));

        fields.add(ofNullable(c.getParentalResponsibilityDetails()));

        return  fields.stream().anyMatch(Optional::isPresent);
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
