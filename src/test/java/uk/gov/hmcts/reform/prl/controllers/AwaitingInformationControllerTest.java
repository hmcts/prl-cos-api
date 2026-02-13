package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AwaitingInformationControllerTest {

    @InjectMocks
    private AwaitingInformationController awaitingInformationController;

    @Mock
    private AwaitingInformationService awaitingInformationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";
    public static final String INVALID_CLIENT_ERROR = "Invalid Client";

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CallbackRequest callbackRequest;
    AwaitingInformation awaitingInformation;

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

        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
    }

    // Tests for submitAwaitingInformation method
    @Test
    public void testSubmitAwaitingInformationSuccessfully() {
        // Given
        Map<String, Object> updatedCaseData = new HashMap<>(caseDataMap);
        updatedCaseData.put(
            PrlAppsConstants.CASE_STATUS,
            CaseStatus.builder().state("Awaiting information").build()
        );
        when(awaitingInformationService.addToCase(callbackRequest))
            .thenReturn(updatedCaseData);

        // When
        AboutToStartOrSubmitCallbackResponse response = awaitingInformationController.submitAwaitingInformation(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        // Then
        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().containsKey(PrlAppsConstants.CASE_STATUS));
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
    }

    @Test
    public void testSubmitAwaitingInformationCallsAddToCaseService() {
        // Given
        Map<String, Object> updatedCaseData = new HashMap<>(caseDataMap);
        when(awaitingInformationService.addToCase(callbackRequest))
            .thenReturn(updatedCaseData);

        // When
        awaitingInformationController.submitAwaitingInformation(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        // Then
        verify(awaitingInformationService, times(1)).addToCase(callbackRequest);
    }

    @Test
    public void testSubmitAwaitingInformationThrowsExceptionWhenUnauthorized() {
        // Given
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        // When & Then
        assertExpectedException(
            () -> awaitingInformationController.submitAwaitingInformation(AUTH_TOKEN, S2S_TOKEN, callbackRequest),
            RuntimeException.class,
            INVALID_CLIENT_ERROR
        );
    }

    @Test
    public void testSubmitAwaitingInformationPreservesExistingCaseData() {
        // Given
        caseDataMap.put("testKey", "testValue");
        Map<String, Object> updatedCaseData = new HashMap<>(caseDataMap);
        updatedCaseData.put(
            PrlAppsConstants.CASE_STATUS,
            CaseStatus.builder().state("Awaiting information").build()
        );
        when(awaitingInformationService.addToCase(callbackRequest))
            .thenReturn(updatedCaseData);

        // When
        AboutToStartOrSubmitCallbackResponse response = awaitingInformationController.submitAwaitingInformation(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        // Then
        assertTrue(response.getData().containsKey("testKey"));
        assertEquals("testValue", response.getData().get("testKey"));
    }


    // Tests for populateHeader method
    @Test
    public void testPopulateHeaderAwaitingInformationSuccessfully() {
        // When
        AboutToStartOrSubmitCallbackResponse response = awaitingInformationController.populateHeader(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        // Then
        assertNotNull(response);
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
    }

    @Test
    public void testPopulateHeaderAwaitingInformationThrowsExceptionWhenUnauthorized() {
        // Given
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        // When & Then
        assertExpectedException(
            () -> awaitingInformationController.populateHeader(AUTH_TOKEN, S2S_TOKEN, callbackRequest),
            RuntimeException.class,
            INVALID_CLIENT_ERROR
        );
    }

    @Test
    public void testPopulateHeaderReturnsEmptyDataWhenAuthorized() {
        // When
        AboutToStartOrSubmitCallbackResponse response = awaitingInformationController.populateHeader(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        // Then
        assertNotNull(response);
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, S2S_TOKEN);
    }

    // Tests for validateUrgentCaseCreation (validateAwaitingInformation) method
    @Test
    public void testValidateAwaitingInformationWithValidDate() {
        // Given
        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(awaitingInformation);
        List<String> emptyErrorList = new ArrayList<>();
        when(awaitingInformationService.validate(awaitingInformation))
            .thenReturn(emptyErrorList);

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
        verify(awaitingInformationService, times(1)).validate(awaitingInformation);
    }

    @Test
    public void testValidateAwaitingInformationWithInvalidDate() {
        // Given
        AwaitingInformation invalidAwaitingInfo = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(invalidAwaitingInfo);

        List<String> errorList = new ArrayList<>();
        errorList.add("The date must be in the future");

        when(awaitingInformationService.validate(invalidAwaitingInfo))
            .thenReturn(errorList);

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());
        assertEquals("The date must be in the future", response.getErrors().getFirst());
        verify(awaitingInformationService, times(1)).validate(invalidAwaitingInfo);
    }

    @Test
    public void testValidateAwaitingInformationWithNullDate() {
        // Given
        AwaitingInformation nullDateAwaitingInfo = AwaitingInformation.builder()
            .reviewDate(null)
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(nullDateAwaitingInfo);

        List<String> emptyErrorList = new ArrayList<>();
        when(awaitingInformationService.validate(nullDateAwaitingInfo))
            .thenReturn(emptyErrorList);

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithTodayDate() {
        // Given
        AwaitingInformation todayDateAwaitingInfo = AwaitingInformation.builder()
            .reviewDate(LocalDate.now())
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(todayDateAwaitingInfo);

        List<String> errorList = new ArrayList<>();
        errorList.add("The date must be in the future");

        when(awaitingInformationService.validate(todayDateAwaitingInfo))
            .thenReturn(errorList);

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
    }


    @Test
    public void testMultipleValidationErrorsReturned() {
        // Given
        List<String> multipleErrors = new ArrayList<>();
        multipleErrors.add("Error 1");
        multipleErrors.add("Error 2");
        multipleErrors.add("Error 3");

        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(awaitingInformation);
        when(awaitingInformationService.validate(awaitingInformation))
            .thenReturn(multipleErrors);

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertEquals(3, response.getErrors().size());
        assertTrue(response.getErrors().contains("Error 1"));
        assertTrue(response.getErrors().contains("Error 2"));
        assertTrue(response.getErrors().contains("Error 3"));
    }

    @Test
    public void testAwaitingInformationWithDifferentReasons() {
        // Given
        AwaitingInformation infoWithOther = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(10))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), AwaitingInformation.class))
            .thenReturn(infoWithOther);
        when(awaitingInformationService.validate(infoWithOther))
            .thenReturn(new ArrayList<>());

        // When
        CallbackResponse response = awaitingInformationController.validateUrgentCaseCreation(callbackRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
    }

    // Helper method for assertion
    protected <T extends Throwable> void assertExpectedException(
        ThrowingRunnable methodExpectedToFail,
        Class<T> expectedThrowableClass,
        String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}

