package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestingSupportCreateCaseDataControllerTest {

    @InjectMocks
    private TestingSupportCreateCaseDataController testingSupportCreateCaseDataController;

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private IdamClient idamClient;
    @Mock
    private CcdCoreCaseDataService coreCaseDataService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CourtNavCaseService courtNavCaseService;


    @Test
    public void shouldCreateCaseWhenCalled() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicantCaseName("test")
            .build();
        Map<String, Object> stringObjectMap = Map.of("id", "1234567891234567");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(coreCaseDataService.eventRequest(any(),any())).thenReturn(EventRequestData.builder().build());
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().build());
        when(coreCaseDataService.startSubmitCreate(any(),any(),any(),anyBoolean())).thenReturn(
            StartEventResponse.builder()
                .token("test")
                .eventId("test")
                .caseDetails(CaseDetails.builder().build()).build());
        when(coreCaseDataService.submitCreate(
            any(),any(),any(),any(),anyBoolean())).thenReturn(
                CaseDetails.builder().build());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(
                1234567891234567L).data(stringObjectMap).build())
            .build();
        CaseDetails caseDetails = testingSupportCreateCaseDataController
            .createCcdTestCase("Bearer:test", "s2s token", callbackRequest);
        assertNotNull(caseDetails);

    }
}
