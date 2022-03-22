package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class C100SubmitApplicationControllerTest {

    @InjectMocks
    C100SubmitApplicationController c100SubmitApplicationController;

    @Mock
    CaseEventService caseEventService;

    @Mock
    SolicitorEmailService solicitorEmailService;

    @Mock
    CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    ObjectMapper objectMapper;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private static final String auth = "auth";


    @Before
    public void init() {
        caseData = CaseData.builder()
            .id(12345L)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

    }

    @Ignore
    @Test
    public void whenLastEventWasSubmitted_thenSubmittedPathFollowed() throws Exception {

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = c100SubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(response.getData().get("state"), State.SUBMITTED_PAID.getValue());
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendEmail(caseDetails);
        verify(allTabService).updateAllTabs(caseData);

    }



}
