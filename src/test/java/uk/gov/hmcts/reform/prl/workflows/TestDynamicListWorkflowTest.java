package uk.gov.hmcts.reform.prl.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.tasks.TestDynamicListTask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TestDynamicListWorkflowTest {

    @Mock
    private TestDynamicListTask testDynamicListTask;

    @InjectMocks
    private TestDynamicListWorkflow testDynamicListWorkflow;

    @Test
    public void give_when_then() throws WorkflowException {

        testDynamicListWorkflow.run(
            CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                                 .data(ImmutableMap.of())
                                 .build())
                .build());

        verify(testDynamicListTask).execute(any(), any());
    }
}
