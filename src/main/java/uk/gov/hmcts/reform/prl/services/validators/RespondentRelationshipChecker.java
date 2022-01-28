package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;


@Service
public class RespondentRelationshipChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished;

        Optional<RespondentRelationObjectType> respondentRelationObjectType = ofNullable(caseData.getRespondentRelationObjectType());
        Optional<RespondentRelationDateInfo> respondentRelationDateInfo = ofNullable(caseData.getRespondentRelationDateInfo());
        Optional<RespondentRelationOptionsInfo> respondentRelationOptionsInfo = ofNullable(caseData.getRespondentRelationOptionsInfo());


        if (respondentRelationObjectType.isPresent()) {

            if (respondentRelationObjectType.get().getApplicantRelationshipEnum().getId().equalsIgnoreCase("noneOfTheAbove")) {
                finished = respondentRelationOptionsInfo.isPresent();
            } else {
                return true;
            }

            if (finished) {
                taskErrorService.removeError(RELATIONSHIP_TO_RESPONDENT);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getRespondentRelationObjectType()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
