package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInitiationControllerTest {

    private final String auth = "testAuth";

    @InjectMocks
    private CaseInitiationController caseInitiationController;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventService;

    @Mock
    EventService eventPublisher;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testHandleSubmitted() {


        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDataMap)
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("testCaseName")
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        caseInitiationController.handleSubmitted(callbackRequest);
        eventService.publishEvent(caseDataChanged);

        verify(applicationsTabService).updateApplicationTabData(caseData);
        verify(eventService).publishEvent(caseDataChanged);

    }
}

