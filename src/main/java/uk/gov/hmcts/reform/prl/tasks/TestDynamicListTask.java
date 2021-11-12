package uk.gov.hmcts.reform.prl.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestDynamicListTask implements Task<WorkflowResult> {

    @Override
    public WorkflowResult execute(TaskContext context, WorkflowResult payload) throws TaskException {

        Map<String, Object> caseData = payload.getCaseData();

        Object applicantName = getApplicant(caseData);

        List<Map<String, Object>> testList = new ArrayList<>();

        Map<String, Object> testApplicantName = new HashMap<>();
        testApplicantName.put(applicantName.toString(), applicantName);

        testList.add(testApplicantName);

        payload.getCaseData().put("testDynamicMultiSelect", testList);

        return payload;
    }

    private String getApplicant(Map<String, Object> caseData) {
        Map<String, Object> applicant =  (Map<String, Object>) caseData.get("Applicant");

        return (String) applicant.get("FirstName");
    }


}

