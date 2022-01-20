package uk.gov.hmcts.reform.prl.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Slf4j
@Component
public class ReturnApplicaitonReturnMessageTask implements Task<CaseDetails> {

    public boolean noRejectReasonSelected(CaseData caseData) {

        boolean noOptionSelected = true;

        Optional<List<RejectReasonEnum>> selectedReason = ofNullable(caseData.getRejectReason());

        boolean hasSelectedOption = allNonEmpty(caseData.getRejectReason());

        if (selectedReason.isPresent() && hasSelectedOption) {
            noOptionSelected = false;
        }

        return noOptionSelected;
    }

    @Override
    public CaseDetails execute(TaskContext context, CaseDetails caseDetails) throws TaskException {
        CaseData caseData = caseDetails.getCaseData();
        if (noRejectReasonSelected(caseData)) {
            log.info("There are no reject reason selected.");
        } else {
            log.info("Preparing pre-filled text for return message");
            List<RejectReasonEnum> listOfReasons = caseData.getRejectReason();

            StringBuilder returnMsgStr = new StringBuilder();

            returnMsgStr.append("Subject line: Application returned: <Case Name>\n")
                .append("Case name: <Case Name>\n")
                .append("Reference code: <Reference>\n\n")
                .append("Dear [Legal representative name],\n\n")
                .append("Thank you for your application."
                            + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

            for (RejectReasonEnum reasonEnum : listOfReasons) {
                returnMsgStr.append(reasonEnum.getReturnMsgText().toString());
            }

            returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
                .append("Kind regards,\n")
                .append("[Name of case worker]");

            caseData.setReturnMessage(returnMsgStr.toString());
        }
        return caseDetails;
    }
}
