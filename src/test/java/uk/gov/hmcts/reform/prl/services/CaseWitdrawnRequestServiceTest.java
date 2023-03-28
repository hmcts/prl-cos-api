package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWitdrawnRequestServiceTest {

    @InjectMocks
    CaseWithdrawnRequestService caseWithdrawnRequestService;

    @Mock
    private ObjectMapper objectMapper;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;


    @Test
    public void testCaseWithdrawnRequestSubmittedInIssueState() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.Yes).build())
            .state(State.CASE_ISSUE)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.CASE_ISSUE.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Requested Application Withdrawal", response.getConfirmationHeader());
    }

    @Test
    public void testCaseWithdrawnRequestSubmittedInGateKeepingState() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.Yes).build())
            .state(State.GATE_KEEPING)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.GATE_KEEPING.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Requested Application Withdrawal", response.getConfirmationHeader());
    }

    @Test
    public void testCaseWithdrawnRequestSubmittedInReturnState() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.Yes).build())
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Requested Application Withdrawal", response.getConfirmationHeader());
    }

    @Test
    public void testCaseWithdrawnRequestSubmittedInPendingState() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.Yes).build())
            .state(State.SUBMITTED_NOT_PAID)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_NOT_PAID.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Application withdrawn", response.getConfirmationHeader());
    }

    @Test
    public void testCaseWithdrawnRequestSubmittedInSubmittedState() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.Yes).build())
            .state(State.SUBMITTED_PAID)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Application withdrawn", response.getConfirmationHeader());
    }

    @Test
    public void testNoCaseWithdrawnRequest() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .withDrawApplicationData(WithdrawApplication.builder().withDrawApplication(YesOrNo.No).build())
            .state(State.SUBMITTED_PAID)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        SubmittedCallbackResponse response = caseWithdrawnRequestService.caseWithdrawnRequestSubmitted(callbackRequest);
        Assert.assertEquals("# Application not withdrawn", response.getConfirmationHeader());
    }
}
