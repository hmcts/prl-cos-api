package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CaseControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestS2sToken";

    @InjectMocks
    private CaseController caseController;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CaseService caseService;

    @Test
    public void shouldCreateCase() {
        //Given
        CaseData caseData = mock(CaseData.class);
        CaseDetails caseDetails = mock(CaseDetails.class);
        Map<String, Object> data = mock(Map.class);
        Mockito.when(caseDetails.getData()).thenReturn(data);
        Mockito.when(objectMapper.convertValue(data, CaseData.class)).thenReturn(caseData);
        Mockito.when(caseService.createCase(caseData, authToken, s2sToken)).thenReturn(caseDetails);

        //When
        CaseData actualCaseData =  caseController.createCase(authToken, s2sToken, caseData);

        //Then
        assertThat(actualCaseData).isEqualTo(caseData);
    }
}
