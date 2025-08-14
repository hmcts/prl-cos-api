package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OtherChildrenNotPartOfTheApplicationChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (!validateFields(caseData)) {
            taskErrorService.addEventError(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                                           OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR,
                                           OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR.getError());
            return false;
        }

        taskErrorService.removeError(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION_ERROR);

        return true;
    }

    public boolean validateOtherChildrenNotInTheCase(CaseData caseData) {
        Optional<List<Element<OtherChildrenNotInTheCase>>> childrenWrapped = ofNullable(caseData.getChildrenNotInTheCase());

        if (!childrenWrapped.isEmpty() && !childrenWrapped.get().isEmpty()) {
            List<OtherChildrenNotInTheCase> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();
            for (OtherChildrenNotInTheCase c : children) {
                log.debug("validateOtherChildrenNotInTheCase - validateMandatoryFieldsCompleted :{} ",validateMandatoryFieldsCompleted(c));
                if (!(validateMandatoryFieldsCompleted(c))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateFields(CaseData caseData) {
        boolean isFinished;
        Optional<YesOrNo> childrenNotPartInTheCaseYesNo = ofNullable(caseData.getChildrenNotPartInTheCaseYesNo());
        if (childrenNotPartInTheCaseYesNo.isPresent() && childrenNotPartInTheCaseYesNo.get().equals(Yes)) {
            return validateOtherChildrenNotInTheCase(caseData);
        } else {
            isFinished = childrenNotPartInTheCaseYesNo.isPresent();
        }
        return isFinished;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesOrNo> childrenNotPartInTheCaseYesNo = ofNullable(caseData.getChildrenNotPartInTheCaseYesNo());

        return childrenNotPartInTheCaseYesNo.isPresent() && childrenNotPartInTheCaseYesNo.get().equals(Yes);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    private boolean validateMandatoryFieldsCompleted(OtherChildrenNotInTheCase child) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(child.getFirstName()));
        fields.add(ofNullable(child.getLastName()));
        if (Yes.equals(child.getIsDateOfBirthKnown())) {
            fields.add(ofNullable(child.getDateOfBirth()));
        }
        Optional<Gender> gender = ofNullable(child.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(other)) {
            fields.add(ofNullable(child.getOtherGender()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
