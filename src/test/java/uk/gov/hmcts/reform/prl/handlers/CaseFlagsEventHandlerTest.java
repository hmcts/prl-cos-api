package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewRaRequestWrapper;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsEventHandlerTest {

    public static final String TEST_AUTH = "test-auth";

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private CaseFlagsWaService caseFlagsWaService;

    @InjectMocks
    private CaseFlagsEventHandler caseFlagsEventHandler;

    @Test
    public void testTriggerDummyEventForCaseFlagsWhenCaseFlagsTaskCreatedIsNoAndNoRequestedCaseFlags() {

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .reviewRaRequestWrapper(ReviewRaRequestWrapper.builder()
                                        .isCaseFlagsTaskCreated(YesOrNo.No).build()).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseFlagsWaService.isCaseHasNoRequestedFlags(any())).thenReturn(false);

        StartAllTabsUpdateDataContent dataContent = new StartAllTabsUpdateDataContent("",
                                                                                      EventRequestData.builder().build(),
                                                                                      StartEventResponse.builder().build(),
                                                                                      new HashMap<>(),
                                                                                      caseData,
                                                                                      UserDetails.builder().build());

        when(allTabService.getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue())))
            .thenReturn(dataContent);

        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(callbackRequest, TEST_AUTH);

        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        verify(allTabService, times(1))
            .getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue()));

        verify(allTabService, times(1))
            .submitAllTabsUpdate(anyString(), anyString(), any(StartEventResponse.class), any(EventRequestData.class), anyMap());
        verify(caseFlagsWaService).isCaseHasNoRequestedFlags(any());

    }

    @Test
    public void testTriggerDummyEventForCaseFlagsWhenCaseFlagsTaskCreatedIsYesAndRequestedCaseFlag() {

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .reviewRaRequestWrapper(ReviewRaRequestWrapper.builder()
                                        .isCaseFlagsTaskCreated(YesOrNo.Yes).build()).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseFlagsWaService.isCaseHasNoRequestedFlags(any())).thenReturn(true);

        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(callbackRequest, TEST_AUTH);

        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        verifyNoInteractions(allTabService);

    }

    @Test
    public void testTriggerDummyEventForCaseFlagsWhenCaseFlagsTaskCreatedIsNoAndRequestedCaseFlag() {

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .reviewRaRequestWrapper(ReviewRaRequestWrapper.builder()
                                        .isCaseFlagsTaskCreated(YesOrNo.No).build()).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseFlagsWaService.isCaseHasNoRequestedFlags(any())).thenReturn(true);

        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(callbackRequest, TEST_AUTH);

        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        verifyNoInteractions(allTabService);

    }

}
