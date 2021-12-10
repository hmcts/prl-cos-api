package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class CaseInitiationControllerTest {

    private final String AUTH = "testAuth";

    @InjectMocks
    CaseInitiationController caseInitiationController;

    @Mock
    EventService eventPublisher;


    @Test
    public void testHandleSubmitted() {

        CaseDataChanged caseDataChanged = new CaseDataChanged(CaseData.builder().build());
        caseInitiationController.publishEvent(caseDataChanged);

        verify(eventPublisher, times(1)).publishEvent(eq(caseDataChanged));

    }

}
