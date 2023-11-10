package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@RunWith(MockitoJUnitRunner.class)
public class AllegationOfHarmRevisedControllerTest {

    @Mock
    AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    private AllegationOfHarmRevisedController allegationOfHarmRevisedController;

    private CaseDetails caseDetails;

    @Test
    public void testPrepopulateChildData() {
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

}
