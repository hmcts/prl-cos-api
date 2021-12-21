package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(SpringRunner.class)
public class AbstractControllerTest {

    @InjectMocks
    AbstractCallbackController abstractCallbackController;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventService eventPublisher;


    @Test
    public void testGetCaseData() {

        CaseDetails caseDetails = CaseDetails.builder().build();
        when(abstractCallbackController.getCaseData(caseDetails)).thenReturn(CaseData.builder().build());
    }

    @Test
    public void testPublishEvent() {

        CaseDataChanged caseDataChanged = new CaseDataChanged(CaseData.builder().build());
        abstractCallbackController.publishEvent(caseDataChanged);
        verify(eventPublisher, times(1)).publishEvent(eq(caseDataChanged));
    }

    @Test
    public void testPublishEvents() {
        CaseDataChanged caseDataChanged1 = new CaseDataChanged(CaseData.builder().build());
        CaseDataChanged caseDataChanged2 = new CaseDataChanged(CaseData.builder().build());
        CaseDataChanged caseDataChanged3 = new CaseDataChanged(CaseData.builder().build());
        CaseDataChanged caseDataChanged4 = new CaseDataChanged(CaseData.builder().build());

        List<Object> listOfEvents = new ArrayList<>();
        listOfEvents.add(caseDataChanged1);
        listOfEvents.add(caseDataChanged2);
        listOfEvents.add(caseDataChanged3);
        listOfEvents.add(caseDataChanged4);

        abstractCallbackController.publishEvents(listOfEvents);
        verify(eventPublisher, times(listOfEvents.size())).publishEvent(CaseDataChanged.class);

    }

}
