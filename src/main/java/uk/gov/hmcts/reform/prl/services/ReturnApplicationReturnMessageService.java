package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.workflows.ReturnApplicationReturnMessageWorkflow;

@Component
@RequiredArgsConstructor
public class ReturnApplicationReturnMessageService {

    private final ReturnApplicationReturnMessageWorkflow returnApplicationReturnMessageWorkflow;

    public CaseData executeReturnMessageWorkflow(CaseDetails caseDetails) throws WorkflowException {

        return returnApplicationReturnMessageWorkflow.run(caseDetails).getCaseData();
    }
}
