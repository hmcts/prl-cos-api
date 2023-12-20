package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RELATIONSHIP_TO_RESPONDENT_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentRelationshipChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished;

        Optional<RespondentRelationObjectType> respondentRelationObjectType = ofNullable(caseData.getRespondentRelationObject());
        Optional<RespondentRelationOptionsInfo> respondentRelationOptionsInfo = ofNullable(caseData.getRespondentRelationOptions());

        if (respondentRelationObjectType.isPresent()) {
            if (respondentRelationObjectType.get().equals(RespondentRelationObjectType.builder()
                                                              .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                                              .build())) {
                finished = respondentRelationOptionsInfo.isPresent();
            } else {
                taskErrorService.removeError(RELATIONSHIP_TO_RESPONDENT_ERROR);
                return true;
            }

            if (finished) {
                taskErrorService.removeError(RELATIONSHIP_TO_RESPONDENT_ERROR);
                return true;
            }
        }
        taskErrorService.addEventError(RELATIONSHIP_TO_RESPONDENT,RELATIONSHIP_TO_RESPONDENT_ERROR,RELATIONSHIP_TO_RESPONDENT_ERROR.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getRespondentRelationObject()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
