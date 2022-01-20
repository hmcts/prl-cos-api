package uk.gov.hmcts.reform.prl.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.TaskContextProvider;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.ConsentOrderNotProvided;

@RunWith(MockitoJUnitRunner.class)
public class ReturnApplicationReturnMessageTaskTest {

    @InjectMocks
    ReturnApplicaitonReturnMessageTask returnMessageTask;

    CaseData casedata;

    @Test
    public void whenNoOptionSelectedThenNoRejectReasonSelectedReturnTrue() {
        casedata = CaseData.builder().build();

        assert returnMessageTask.noRejectReasonSelected(casedata);
    }

    @Test
    public void whenHasOptionSelectedThenNoRejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .rejectReason(Collections.singletonList(ConsentOrderNotProvided))
            .build();

        assert !returnMessageTask.noRejectReasonSelected(casedata);
    }

    @Test
    public void executeReturnMessageService() {
        returnMessageTask.execute(TaskContextProvider.empty(), CaseDetailsProvider.empty());

        casedata = CaseData.builder()
            .rejectReason(Collections.singletonList(ConsentOrderNotProvided))
            .build();

        StringBuilder returnMsgStr = new StringBuilder();

        returnMsgStr.append("Subject line: Application returned: <Case Name>\n")
            .append("Case name: <Case Name>\n")
            .append("Reference code: <Reference>\n\n")
            .append("Dear [Legal representative name],\n\n")
            .append("Thank you for your application."
                        + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

        returnMsgStr.append(ConsentOrderNotProvided.getReturnMsgText().toString());

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append("[Name of case worker]");

        casedata.setReturnMessage(returnMsgStr.toString());

        assertEquals(returnMsgStr.toString(), casedata.getReturnMessage());
    }
}
