package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.C100IssueCaseService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class C100IssueCaseControllerTest {

    @InjectMocks
    private C100IssueCaseController c100IssueCaseController;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private AuthorisationService authorisationService;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    void testIssueAndSendLocalCourt() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        when(c100IssueCaseService.issueAndSendToLocalCourt(
            any(String.class),
            any(CallbackRequest.class)
        )).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = c100IssueCaseController
            .issueAndSendToLocalCourt(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertEquals(stringObjectMap, aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    void testExceptionForIssueAndSendToLocalCourt() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () ->
                c100IssueCaseController.issueAndSendToLocalCourt(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testIssueAndSendLocalCourtNotification() {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        doNothing().when(c100IssueCaseService).issueAndSendToLocalCourNotification(
            any(CallbackRequest.class));
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        CallbackResponse aboutToStartOrSubmitCallbackResponse = c100IssueCaseController
            .issueAndSendToLocalCourtNotification(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }

    @Test
    void testExceptionForIssueAndSendToLocalCourtNotification() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () ->
                c100IssueCaseController.issueAndSendToLocalCourtNotification(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
