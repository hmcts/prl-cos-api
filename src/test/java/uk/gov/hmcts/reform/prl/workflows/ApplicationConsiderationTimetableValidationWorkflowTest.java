package uk.gov.hmcts.reform.prl.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableEffortsValidationTask;
import uk.gov.hmcts.reform.prl.tasks.ApplicationTimetableTimeValidationTask;
import wiremock.org.apache.commons.lang3.reflect.FieldUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConsiderationTimetableValidationWorkflowTest {

    @Mock
    private ApplicationTimetableTimeValidationTask applicationTimetableTimeValidationTask;
    @Mock
    private ApplicationTimetableEffortsValidationTask applicationTimetableEffortsValidationTask;

    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private ApplicationConsiderationTimetableValidationWorkflow applicationConsiderationTimetableValidationWorkflow;

    @BeforeEach
    void init_mocks() throws IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        FieldUtils.writeField(applicationConsiderationTimetableValidationWorkflow, "objectMapper", objectMapper, true);
    }


    @Test
    public void whenWorkflowRun_thenExpectedTasksInvoked() throws WorkflowException {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();

        objectMapper.convertValue(caseData, Map.class);
        applicationConsiderationTimetableValidationWorkflow.run(
            CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                                 .data(objectMapper.convertValue(caseData, Map.class))
                                 .build())
                .build());

        verify(applicationTimetableTimeValidationTask).execute(any(), any());
        verify(applicationTimetableEffortsValidationTask).execute(any(), any());
    }
}
