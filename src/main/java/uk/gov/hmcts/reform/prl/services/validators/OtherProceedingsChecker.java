package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.DONT_KNOW;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.NO;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class OtherProceedingsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;


    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<YesNoDontKnow> otherProceedings = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        boolean otherProceedingsCompleted = otherProceedings.isPresent();

        if (otherProceedingsCompleted
            && (otherProceedings.get().equals(NO) || otherProceedings.get().equals(DONT_KNOW))) {
            taskErrorService.removeError(OTHER_PROCEEDINGS_ERROR);
            return  true;
        }

        Optional<List<Element<ProceedingDetails>>> proceedingDetails = ofNullable(caseData.getExistingProceedings());

        if (proceedingDetails.isPresent()) {
            List<ProceedingDetails> allProceedings = proceedingDetails.get()
                                                .stream()
                                                .map(Element::getValue)
                                                .collect(Collectors.toList());

            //if a collection item is added and then removed the collection exists as length 0
            if (allProceedings.size() == 0) {
                return false;
            }


            boolean allMandatoryFieldsDone = true;

            for (ProceedingDetails proceeding : allProceedings) {
                Optional<ProceedingsEnum> previousOrCurrent = ofNullable(proceeding.getPreviousOrOngoingProceedings());

                if (previousOrCurrent.isEmpty()) {
                    allMandatoryFieldsDone = false;
                    break;
                }

                allMandatoryFieldsDone = validateMandatoryProceedingDetailsForOtherProceedings(proceeding);

            }
            if (allMandatoryFieldsDone) {
                taskErrorService.removeError(OTHER_PROCEEDINGS_ERROR);
                return true;
            }
        }
        return false;

    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesNoDontKnow> otherProceedings = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());

        if (otherProceedings.isPresent() && otherProceedings.get().equals(YES)) {
            taskErrorService.addEventError(OTHER_PROCEEDINGS, OTHER_PROCEEDINGS_ERROR,
                                           OTHER_PROCEEDINGS_ERROR.getError());
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean validateMandatoryProceedingDetailsForOtherProceedings(ProceedingDetails proceeding) {

        boolean fields = allNonEmpty(
            proceeding.getNameOfCourt()
        );
        return fields;
    }
}
