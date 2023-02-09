package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    AssignCaseAccessService assignCaseAccessService;

    @Mock
    EventService eventPublisher;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void testHandleSubmitted() {


        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");
        String userID = "12345";

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

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        doNothing().when(assignCaseAccessService).assignCaseAccess(String.valueOf(caseData.getId()),auth);

        caseInitiationController.handleSubmitted(auth,callbackRequest);
        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        eventService.publishEvent(caseDataChanged);

        applicationsTabService.updateTab(caseData);
        verify(applicationsTabService).updateTab(caseData);
        verify(eventService).publishEvent(caseDataChanged);

    }
}

