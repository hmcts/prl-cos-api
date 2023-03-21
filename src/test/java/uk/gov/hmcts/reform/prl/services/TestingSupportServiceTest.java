package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestingSupportServiceTest {

    @InjectMocks
    TestingSupportService testingSupportService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EventService eventPublisher;
    @Mock
    private AllTabServiceImpl tabService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentGenService dgsService;

    @Mock
    private CaseUtils caseUtils;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

    @Test
    public void testAboutToSubmitCaseCreationWithoutDummyData() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
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
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(callbackRequest);
        Assert.assertTrue(stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitCaseCreationWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), Mockito.any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitCaseCreationWithDummyFl401Data() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), Mockito.any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }
}
