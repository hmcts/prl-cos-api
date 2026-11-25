package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.RequestFurtherInformationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RequestFurtherInformationControllerTest {

    @InjectMocks
    private RequestFurtherInformationController requestFurtherInformationController;

    @Mock
    private RequestFurtherInformationService requestFurtherInformationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    private static final String AUTH_TOKEN = "Bearer testAuthToken";
    private static final String S2S_TOKEN = "s2sTestToken";

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;
    private Map<String, Object> updatedCaseData;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);

        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state("AWAITING_INFORMATION")
            .data(caseDataMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        updatedCaseData = new HashMap<>(caseDataMap);
        updatedCaseData.put(CASE_STATUS, CaseStatus.builder().state("Awaiting information").build());

        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
    }

    // Tests for submitAwaitingInformation endpoint

    @Test
    public void shouldSubmitAwaitingInformationSuccessfully() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().containsKey(CASE_STATUS));
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
        verify(requestFurtherInformationService, times(1)).addToCase(callbackRequest);
    }

    @Test
    public void shouldThrowExceptionWhenUnauthorized() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        assertEquals("Invalid Client", exception.getMessage());
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
        verify(requestFurtherInformationService, times(0)).addToCase(any());
    }

    @Test
    public void shouldThrowExceptionWhenFeatureToggleDisabled() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        assertEquals("Invalid Client", exception.getMessage());
        verify(requestFurtherInformationService, times(0)).addToCase(any());
    }

    @Test
    public void shouldPreserveExistingCaseDataWhenSubmitting() {
        caseDataMap.put("applicantName", "John Doe");
        caseDataMap.put("respondentName", "Jane Doe");
        updatedCaseData.put("applicantName", "John Doe");
        updatedCaseData.put("respondentName", "Jane Doe");

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response.getData());
        assertEquals("John Doe", response.getData().get("applicantName"));
        assertEquals("Jane Doe", response.getData().get("respondentName"));
    }

    @Test
    public void shouldHandleNullCaseData() {
        Map<String, Object> emptyCaseData = new HashMap<>();
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(emptyCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
    }

    // Tests for validateReviewDate endpoint

    @Test
    public void shouldValidateReviewDateSuccessfully() {
        List<String> emptyErrors = new ArrayList<>();
        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(emptyErrors);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
        verify(requestFurtherInformationService, times(1)).validate(callbackRequest);
    }

    @Test
    public void shouldReturnValidationErrorsForInvalidDate() {
        List<String> errorList = new ArrayList<>();
        errorList.add("Please enter a future date");

        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(errorList);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals("Please enter a future date", response.getErrors().get(0));
    }

    @Test
    public void shouldReturnEmptyErrorsWhenFeatureToggleDisabled() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void shouldReturnMultipleValidationErrors() {
        List<String> errorList = new ArrayList<>();
        errorList.add("Please enter a future date");
        errorList.add("Review date cannot be more than 12 months away");

        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(errorList);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        assertEquals(2, response.getErrors().size());
        verify(requestFurtherInformationService, times(1)).validate(callbackRequest);
    }

    @Test
    public void shouldCallFeatureToggleServiceBeforeValidation() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(new ArrayList<>());

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        verify(featureToggleService, times(1)).isAwaitingInformationEnabled();
    }

    @Test
    public void shouldHandleCaseDataWithMultipleFields() {
        caseDataMap.put("caseType", "C100");
        caseDataMap.put("eventId", "123456");
        updatedCaseData.put("caseType", "C100");
        updatedCaseData.put("eventId", "123456");

        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response.getData());
        assertEquals("C100", response.getData().get("caseType"));
        assertEquals("123456", response.getData().get("eventId"));
    }

    @Test
    public void shouldVerifyCorrectHeadersUsedInSubmit() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(updatedCaseData);

        requestFurtherInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        verify(authorisationService, times(1)).isAuthorized(eq(AUTH_TOKEN), eq(S2S_TOKEN));
    }


    @Test
    public void historyUpdated_ShouldSuccessfullyUpdateHistoryTab() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        var response = requestFurtherInformationController.historyUpdated(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(requestFurtherInformationService, times(1)).updateHistoryTab(callbackRequest);
    }

    @Test
    public void historyUpdated_ThrowsExceptionWhenUnauthorized() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.historyUpdated(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        verify(requestFurtherInformationService, times(0)).updateHistoryTab(any());
    }

    @Test
    public void historyUpdated_ThrowsExceptionWhenFeatureToggleDisabled() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.historyUpdated(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        verify(requestFurtherInformationService, times(0)).updateHistoryTab(any());
    }

    @Test
    public void submitAwaitingInformation_WithValidTokens_Success() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(updatedCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response);
        assertNotNull(response.getData());
    }

    @Test
    public void validateReviewDate_FeatureToggleEnabled_CallsService() {
        List<String> emptyErrors = new ArrayList<>();
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(emptyErrors);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        verify(requestFurtherInformationService, times(1)).validate(callbackRequest);
        verify(featureToggleService, times(1)).isAwaitingInformationEnabled();
    }

    @Test
    public void submitAwaitingInformation_AuthorizationFails_ThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        verify(requestFurtherInformationService, times(0)).addToCase(callbackRequest);
    }

    @Test
    public void submitAwaitingInformation_FeatureToggleDisabled_ThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
            requestFurtherInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

        verify(requestFurtherInformationService, times(0)).addToCase(callbackRequest);
    }

    @Test
    public void validateReviewDate_MultipleErrors_ReturnsAllErrors() {
        List<String> errorList = new ArrayList<>();
        errorList.add("Error 1");
        errorList.add("Error 2");
        errorList.add("Error 3");

        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(errorList);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertEquals(3, response.getErrors().size());
    }

    @Test
    public void submitAwaitingInformation_EmptyCaseData_ReturnsResponse() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        Map<String, Object> emptyCaseData = new HashMap<>();
        when(requestFurtherInformationService.addToCase(callbackRequest)).thenReturn(emptyCaseData);

        AboutToStartOrSubmitCallbackResponse response = requestFurtherInformationController
            .submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    public void historyUpdated_WithValidTokens_ReturnsOkStatus() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);

        var response = requestFurtherInformationController.historyUpdated(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void validateReviewDate_FeatureDisabledAndErrorsExist_ReturnsEmptyErrors() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertTrue(response.getErrors().isEmpty());
        // Service should not be called when feature is disabled
        verify(requestFurtherInformationService, times(0)).validate(any());
    }


    @Test
    public void validateReviewDate_NoErrorsReturned_ResponseIsEmpty() {
        when(requestFurtherInformationService.validate(callbackRequest)).thenReturn(Collections.emptyList());

        CallbackResponse response = requestFurtherInformationController.validateReviewDate(callbackRequest);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(0, response.getErrors().size());
    }
}
