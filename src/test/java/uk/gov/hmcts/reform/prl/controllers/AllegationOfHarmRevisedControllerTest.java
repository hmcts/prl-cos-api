package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllegationOfHarmRevisedControllerTest {

    @InjectMocks
    private AllegationOfHarmRevisedController allegationOfHarmRevisedController;

    @Mock
    AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    private CaseDetails caseDetails;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    void testPrepopulateChildData() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .state("CASE_ISSUED")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        String authorisation = "authorisation";
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CaseData caseData1 = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.valueOf(caseDetails.getState()))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        allegationOfHarmRevisedController.prePopulateChildData(authorisation, callbackRequest);
        verify(allegationOfHarmRevisedService, times(1))
            .getPrePopulatedChildData(caseData1);

    }

    @Test
    void testMidEvent() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .state("CASE_ISSUED")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        CaseData caseData1 = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.valueOf(caseDetails.getState()))
            .build();
        allegationOfHarmRevisedController.handleMidEvent(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(allegationOfHarmRevisedService, times(1))
            .resetFields(caseData1, stringObjectMap);
    }
}
