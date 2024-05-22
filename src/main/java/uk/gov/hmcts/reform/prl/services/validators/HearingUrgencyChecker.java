package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HEARING_URGENCY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingUrgencyChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        List<Optional<?>> fields = new ArrayList<>();

        Optional<YesOrNo> isCaseUrgent = ofNullable(caseData.getIsCaseUrgent());
        fields.add(isCaseUrgent);
        if (isCaseUrgent.isPresent() && isCaseUrgent.get().equals(Yes)) {
            fields.add(ofNullable(caseData.getCaseUrgencyTimeAndReason()));
            fields.add(ofNullable(caseData.getEffortsMadeWithRespondents()));
        }
        Optional<YesOrNo> withoutNoticeHearing = ofNullable(caseData.getDoYouNeedAWithoutNoticeHearing());
        fields.add(withoutNoticeHearing);
        if (withoutNoticeHearing.isPresent() && withoutNoticeHearing.get().equals(Yes)) {
            fields.add(ofNullable(caseData.getReasonsForApplicationWithoutNotice()));
        }
        Optional<YesOrNo> reducedNoticeHearing = ofNullable(caseData.getDoYouRequireAHearingWithReducedNotice());
        fields.add(reducedNoticeHearing);
        if (reducedNoticeHearing.isPresent() && reducedNoticeHearing.get().equals(Yes)) {
            fields.add(ofNullable(caseData.getSetOutReasonsBelow()));
        }
        fields.add(ofNullable(caseData.getAreRespondentsAwareOfProceedings()));

        boolean finished = fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

        if (finished || hasMandatoryCompleted(caseData)) {
            taskErrorService.removeError(HEARING_URGENCY_ERROR);
            return true;
        } else {
            taskErrorService.addEventError(HEARING_URGENCY,
                                           HEARING_URGENCY_ERROR,
                                           HEARING_URGENCY_ERROR.getError());
            return false;
        }
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
        return  false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
