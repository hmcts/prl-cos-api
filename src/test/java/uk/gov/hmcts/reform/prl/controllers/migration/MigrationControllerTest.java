package uk.gov.hmcts.reform.prl.controllers.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class MigrationControllerTest {

    @Mock
    AllTabServiceImpl tabService;

    @InjectMocks
    MigrationController migrationController;


    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

    @Mock
    private ObjectMapper objectMapper;


    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
                .id(12345678L)
                .state(State.JUDICIAL_REVIEW)
                .build();
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.JUDICIAL_REVIEW.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    @Test
    public void handleSubmitted() {
        migrationController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    }

    @Test
    public void handleSubmittedStateRemains() {
        caseData = CaseData.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID)
                .build();
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();
        migrationController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.any(CaseData.class));
    }

    @Test
    public void handleAboutToSubmitted() {
        AboutToStartOrSubmitCallbackResponse response = migrationController.handleAboutToSubmitted(callbackRequest, "testAuth");
        Assert.assertNotNull(response.getState());
    }

    @Test
    public void handleAboutToSubmittedNullState() {

        caseData = CaseData.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID)
                .build();
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();
        AboutToStartOrSubmitCallbackResponse response = migrationController.handleAboutToSubmitted(callbackRequest, "testAuth");
        Assert.assertNull(response.getState());
    }
}