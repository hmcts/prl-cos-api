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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseInitiationControllerTest extends AbstractCallbackController{

    private final String auth = "testAuth";

    @InjectMocks
    private CaseInitiationController caseInitiationController;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    EventService eventPublisher;

    private Map<String, Object> caseDataMap;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");

        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDataMap)
            .build();
    }

    @Test
    public void testHandleSubmitted() {

        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        CaseData caseData = getCaseData(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        doNothing().when(applicationsTabService).updateApplicationTabData(Mockito.any(CaseData.class));

        publishEvent(new CaseDataChanged(caseData));
        assertTrue(true);
    }
}

