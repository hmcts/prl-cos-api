package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HEARING_URGENCY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class HearingUrgencyChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getIsCaseUrgent() != null) {
            if (caseData.getIsCaseUrgent().equals(NO)) {

                boolean noOptionCompleted = allNonEmpty(
                    caseData.getDoYouNeedAWithoutNoticeHearing(),
                    caseData.getDoYouRequireAHearingWithReducedNotice(),
                    caseData.getAreRespondentsAwareOfProceedings()
                );
                if (noOptionCompleted) {
                    taskErrorService.removeError(HEARING_URGENCY_ERROR);
                    return true;
                }

            } else {
                boolean yesOptionCompleted =  allNonEmpty(
                    caseData.getCaseUrgencyTimeAndReason(),
                    caseData.getEffortsMadeWithRespondents(),
                    caseData.getDoYouNeedAWithoutNoticeHearing(),
                    caseData.getReasonsForApplicationWithoutNotice(),
                    caseData.getDoYouRequireAHearingWithReducedNotice(),
                    caseData.getSetOutReasonsBelow(),
                    caseData.getAreRespondentsAwareOfProceedings()
                );

                if (yesOptionCompleted) {
                    taskErrorService.removeError(HEARING_URGENCY_ERROR);
                    return true;
                }
            }
        }
        taskErrorService.addEventError(HEARING_URGENCY,
                                       HEARING_URGENCY_ERROR,
                                       HEARING_URGENCY_ERROR.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getCaseUrgencyTimeAndReason(),
            caseData.getEffortsMadeWithRespondents(),
            caseData.getDoYouNeedAWithoutNoticeHearing(),
            caseData.getReasonsForApplicationWithoutNotice(),
            caseData.getDoYouRequireAHearingWithReducedNotice(),
            caseData.getSetOutReasonsBelow(),
            caseData.getAreRespondentsAwareOfProceedings()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        if (caseData.getIsCaseUrgent() != null) {
            switch (caseData.getIsCaseUrgent()) {
                case YES:
                    List<Object> mandatoryFields = new ArrayList<>();
                    mandatoryFields.add(caseData.getCaseUrgencyTimeAndReason());
                    mandatoryFields.add(caseData.getEffortsMadeWithRespondents());
                    mandatoryFields.add(caseData.getDoYouNeedAWithoutNoticeHearing());
                    mandatoryFields.add(caseData.getReasonsForApplicationWithoutNotice());
                    mandatoryFields.add(caseData.getDoYouRequireAHearingWithReducedNotice());
                    mandatoryFields.add(caseData.getSetOutReasonsBelow());

                    boolean fieldsComplete = true;

                    for (Object field : mandatoryFields) {
                        if (field == null) {
                            fieldsComplete = false;
                        }
                        return fieldsComplete;
                    }
                case NO:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

}
