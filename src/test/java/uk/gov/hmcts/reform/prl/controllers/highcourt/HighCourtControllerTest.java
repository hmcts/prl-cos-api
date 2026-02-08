package uk.gov.hmcts.reform.prl.controllers.highcourt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.highcourt.HighCourtService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HighCourtControllerTest {
    @InjectMocks
    private HighCourtController highCourtController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private HighCourtService highCourtService;

    private static final String AUTH_TOKEN = "auth-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Test
    public void handleSubmittedSuccess() {
        Map caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("isHighCourtCase", "Yes");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);

        highCourtController.handleSubmitted(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);

        verify(highCourtService, times(1)).setCaseAccess(caseDetails);
    }

    @Test
    public void shouldNothandleSubmittedWhenNotAuthorised() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);

        assertThrows(
            RuntimeException.class,
            () -> highCourtController
                .handleSubmitted(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest));
    }
}
