package uk.gov.hmcts.reform.prl.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;

import static org.junit.Assert.assertTrue;

public class TestDynamicListTaskTest {

    private TestDynamicListTask testDynamicListTask =
        new TestDynamicListTask();


    @Test
    public void givenValidCaseData_thenListOptionAdded() {

        ImmutableMap<String, Object> testData = ImmutableMap.of(
            "FirstName", "TestFirst",
            "LastName", "TestLast"
        );

        WorkflowResult workflowData = new WorkflowResult(ImmutableMap.of(
            "Applicant", testData));

        WorkflowResult workflowResult = testDynamicListTask.execute(new DefaultTaskContext(), workflowData);
        assertTrue(workflowData.getCaseData().get("Applicant").equals(testData));
    }
}
