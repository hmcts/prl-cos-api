package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenEmailService;

import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CaseSubmissionNotificationCallbackControllerTest {

    private static final String authToken = "Bearer TestAuthToken";
    private static final Long caseId = 123456L;

    @Mock
    private CitizenEmailService citizenEmailService;

    @InjectMocks
    private CaseSubmissionNotificationCallbackController caseSubmissionNotificationCallbackController;

    @Test
    public void testNotification() {
        //Given
        CaseData caseData = CaseData.builder().id(caseId).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(caseId)
                        .data(stringObjectMap).build()).build();

        //When
        caseSubmissionNotificationCallbackController.handleNotification(authToken, callbackRequest);

        //Then
        verify(citizenEmailService).sendCitizenCaseSubmissionEmail(authToken, String.valueOf(caseId));
    }
}