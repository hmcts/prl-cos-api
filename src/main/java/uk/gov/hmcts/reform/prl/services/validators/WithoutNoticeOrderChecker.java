package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.WITHOUT_NOTICE_ORDER_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WithoutNoticeOrderChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished;
        Optional<WithoutNoticeOrderDetails> withoutNoticeOrderObj = ofNullable(caseData.getOrderWithoutGivingNoticeToRespondent());
        if (withoutNoticeOrderObj.isPresent() && withoutNoticeOrderObj.get() != null) {
            Optional<YesOrNo> isWithoutNoticeOrderApplied = ofNullable(withoutNoticeOrderObj.get().getOrderWithoutGivingNotice());

            if (isWithoutNoticeOrderApplied.isPresent() && isWithoutNoticeOrderApplied.get().equals(Yes)) {
                Optional<List<ReasonForOrderWithoutGivingNoticeEnum>> reasonForOrderWithoutGivingNotice =
                    ofNullable(caseData.getReasonForOrderWithoutGivingNotice().getReasonForOrderWithoutGivingNotice());

                Optional<YesNoDontKnow> bailConditionDetails = ofNullable(
                    caseData.getBailDetails().getIsRespondentAlreadyInBailCondition());

                finished = (reasonForOrderWithoutGivingNotice.isPresent() && !reasonForOrderWithoutGivingNotice.get().equals(Collections.emptyList()))
                    && bailConditionDetails.isPresent();

                if (finished) {
                    taskErrorService.removeError(WITHOUT_NOTICE_ORDER_ERROR);
                    return true;
                }
            } else if ((isWithoutNoticeOrderApplied.isPresent() && isWithoutNoticeOrderApplied.get().equals(No))) {
                taskErrorService.removeError(WITHOUT_NOTICE_ORDER_ERROR);
                return true;
            }
        }
        taskErrorService.addEventError(WITHOUT_NOTICE_ORDER, WITHOUT_NOTICE_ORDER_ERROR, WITHOUT_NOTICE_ORDER_ERROR.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<WithoutNoticeOrderDetails> orderWithoutGivingNoticeToRespondent = ofNullable(caseData.getOrderWithoutGivingNoticeToRespondent());
        return (orderWithoutGivingNoticeToRespondent.isPresent()
            && orderWithoutGivingNoticeToRespondent.get() != null
            && orderWithoutGivingNoticeToRespondent.get().getOrderWithoutGivingNotice() != null);
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

