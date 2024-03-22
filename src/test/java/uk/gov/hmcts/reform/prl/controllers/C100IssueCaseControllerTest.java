package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.C100IssueCaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100IssueCaseControllerTest {

    public static final String authToken = "Bearer TestAuthToken";

    @InjectMocks
    private C100IssueCaseController c100IssueCaseController;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String s2sToken = "s2s AuthToken";

    @Test
    public void testIssueAndSendLocalCourt() throws Exception {

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
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = c100IssueCaseController
            .issueAndSendToLocalCourt(authToken,s2sToken,callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertEquals(stringObjectMap, aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testExceptionForIssueAndSendToLocalCourt() throws Exception {
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
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100IssueCaseController
                .issueAndSendToLocalCourt(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testIssueAndSendLocalCourtNotification() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();
        doNothing().when(c100IssueCaseService).issueAndSendToLocalCourNotification(
            any(CallbackRequest.class));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackResponse aboutToStartOrSubmitCallbackResponse = c100IssueCaseController
            .issueAndSendToLocalCourtNotification(authToken,s2sToken,callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }

    @Test
    public void testExceptionForIssueAndSendToLocalCourtNotification() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        doNothing().when(c100IssueCaseService).issueAndSendToLocalCourNotification(
            any(CallbackRequest.class));
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100IssueCaseController
                .issueAndSendToLocalCourtNotification(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}