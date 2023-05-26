package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseDeletionControllerTest {

    @InjectMocks
    private CaseDeletionController caseDeletionController;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void shouldRemoveAllCaseDetailsWhenCalled() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse =  caseDeletionController
            .handleAboutToSubmitEvent(authToken,s2sToken,callbackRequest);

        assertThat(callbackResponse.getData()).isEmpty();
    }

}
