package uk.gov.hmcts.reform.prl.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

@Component
public class CreatePaymentServiceRequestTask implements Task<WorkflowResult> {

    @Autowired
    private ObjectMapper objectMapper;




    @Override
    public WorkflowResult execute(TaskContext context, WorkflowResult payload) throws TaskException {

        CaseData caseData = objectMapper.convertValue(payload.getCaseData(), CaseData.class);


       // System.out.println("**********************"+paymentServiceResponse.getServiceRequestReference());
        return payload;
    }
}

