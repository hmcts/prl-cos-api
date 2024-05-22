package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.dontKnow;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.no;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OtherProceedingsChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<YesNoDontKnow> otherProceedings = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        boolean otherProceedingsCompleted = otherProceedings.isPresent();

        if (otherProceedingsCompleted
            && (otherProceedings.get().equals(no) || otherProceedings.get().equals(dontKnow))) {
            taskErrorService.removeError(OTHER_PROCEEDINGS_ERROR);
            return  true;
        }

        Optional<List<Element<ProceedingDetails>>> proceedingDetails = ofNullable(caseData.getExistingProceedings());

        if (proceedingDetails.isPresent()) {
            List<ProceedingDetails> allProceedings = proceedingDetails.get()
                .stream()
                .map(Element::getValue)
                .toList();

            //if a collection item is added and then removed the collection exists as length 0
            if (allProceedings.isEmpty()) {
                return false;
            }


            boolean allMandatoryFieldsDone = true;

            for (ProceedingDetails proceeding : allProceedings) {
                Optional<ProceedingsEnum> previousOrCurrent = ofNullable(proceeding.getPreviousOrOngoingProceedings());

                if (previousOrCurrent.isEmpty()) {
                    allMandatoryFieldsDone = false;
                    break;
                }

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

        if (otherProceedings.isPresent() && otherProceedings.get().equals(yes)) {
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

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
