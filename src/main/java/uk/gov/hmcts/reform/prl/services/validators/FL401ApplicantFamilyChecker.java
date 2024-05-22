package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_APPLICANT_FAMILY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicantFamilyChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        boolean finished = validateObjectFields(caseData);

        if (finished) {
            taskErrorService.removeError(FL401_APPLICANT_FAMILY_ERROR);
            return true;
        }
        taskErrorService.addEventError(FL401_APPLICANT_FAMILY_DETAILS, FL401_APPLICANT_FAMILY_ERROR,
                                           FL401_APPLICANT_FAMILY_ERROR.getError()
        );
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<ApplicantFamilyDetails> applicantFamilyDetailsWrapped = ofNullable(caseData.getApplicantFamilyDetails());

        if (!applicantFamilyDetailsWrapped.isEmpty()) {
            Optional<YesOrNo> doesApplicantHasChild = ofNullable(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren());
            if (!doesApplicantHasChild.isEmpty() && Yes.equals(doesApplicantHasChild.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean validateObjectFields(CaseData caseData) {
        boolean isFinished = false;

        Optional<ApplicantFamilyDetails> applicantFamilyDetailsWrapped = ofNullable(caseData.getApplicantFamilyDetails());

        if (applicantFamilyDetailsWrapped.isPresent()) {
            isFinished = validateFields(caseData);
        }
        return isFinished;
    }

    public boolean validateFields(CaseData caseData) {
        boolean isFinished = false;

        Optional<YesOrNo> doesApplicantHasChild = ofNullable(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren());

        if (!doesApplicantHasChild.isEmpty() && Yes.equals(doesApplicantHasChild.get())) {

            Optional<List<Element<ApplicantChild>>> applicantChildrenWrapped
                = ofNullable(caseData.getApplicantChildDetails());

            if (!applicantChildrenWrapped.isEmpty() && !applicantChildrenWrapped.get().isEmpty()) {
                List<ApplicantChild> applicantChildren = applicantChildrenWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .toList();

                for (ApplicantChild ac : applicantChildren) {
                    if (!(validateMandatoryFieldsCompleted(ac))) {
                        isFinished = false;
                    } else {
                        isFinished = true;
                    }
                }
            }
        } else {
            isFinished = doesApplicantHasChild.isPresent();
        }
        return isFinished;
    }

    public boolean validateMandatoryFieldsCompleted(ApplicantChild applicantChild) {
        Optional<YesOrNo> applicantRespondentShareParental = ofNullable(applicantChild.getApplicantRespondentShareParental());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(applicantChild.getFullName()));
        fields.add(ofNullable(applicantChild.getDateOfBirth()));
        fields.add(ofNullable(applicantChild.getApplicantChildRelationship()));
        fields.add(applicantRespondentShareParental);

        if (applicantRespondentShareParental.isPresent() && applicantRespondentShareParental.get().equals(Yes)) {
            fields.add(ofNullable(applicantChild.getRespondentChildRelationship()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
