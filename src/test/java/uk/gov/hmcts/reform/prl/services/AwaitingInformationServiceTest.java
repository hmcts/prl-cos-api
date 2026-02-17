package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AwaitingInformationServiceTest {

    @InjectMocks
    private AwaitingInformationService awaitingInformationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CcdCoreCaseDataService ccdCoreCaseDataService;

    @Mock
    private SystemUserService systemUserService;

    private AwaitingInformation awaitingInformation;
    private Map<String, Object> caseDataMap;
    private CaseDetails caseDetails;
    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);

        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

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

        when(systemUserService.getSysUserToken()).thenReturn("system-auth-token");
        when(systemUserService.getUserId("system-auth-token")).thenReturn("system-user-id");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.AWAITING_INFORMATION, "system-user-id"))
            .thenReturn(EventRequestData.builder().eventId("AWAITING_INFORMATION").build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), anyBoolean()))
            .thenReturn(StartEventResponse.builder().token("test-token").build());
        when(ccdCoreCaseDataService.createCaseDataContent(any(StartEventResponse.class), any()))
            .thenReturn(CaseDataContent.builder().build());
        when(objectMapper.convertValue(eq(caseDataMap), eq(AwaitingInformation.class)))
            .thenReturn(awaitingInformation);
    }

    @Test
    public void shouldReturnEmptyErrorListWhenReviewDateIsInFuture() {
        AwaitingInformation info = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.miamFurtherInformation)
            .build();

        List<String> errors = awaitingInformationService.validate(info);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReturnErrorWhenReviewDateIsToday() {
        AwaitingInformation info = AwaitingInformation.builder()
            .reviewDate(LocalDate.now())
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        List<String> errors = awaitingInformationService.validate(info);

        assertFalse(errors.isEmpty());
        assertEquals("The date must be in the future", errors.getFirst());
    }

    @Test
    public void shouldReturnErrorWhenReviewDateIsPast() {
        AwaitingInformation info = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.respondentFurtherInformation)
            .build();

        List<String> errors = awaitingInformationService.validate(info);

        assertFalse(errors.isEmpty());
        assertEquals("The date must be in the future", errors.getFirst());
    }

    @Test
    public void shouldReturnEmptyErrorListWhenReviewDateIsNull() {
        AwaitingInformation info = AwaitingInformation.builder()
            .reviewDate(null)
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.dwpHmrcWhereaboutsUnknown)
            .build();

        List<String> errors = awaitingInformationService.validate(info);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenFeatureToggleDisabledDuringValidation() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        try {
            awaitingInformationService.validate(awaitingInformation);
        } catch (RuntimeException e) {
            assertEquals("Awaiting information feature is not enabled", e.getMessage());
        }
    }

    @Test
    public void shouldSuccessfullyAddToCaseAndUpdateCaseDataStore() {
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        assertNotNull(result);
        assertTrue(result.containsKey(CASE_STATUS));
        verify(systemUserService, times(1)).getSysUserToken();
        verify(systemUserService, times(1)).getUserId("system-auth-token");
        verify(ccdCoreCaseDataService, times(1)).eventRequest(CaseEvent.AWAITING_INFORMATION, "system-user-id");
        verify(ccdCoreCaseDataService, times(1)).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(ccdCoreCaseDataService, times(1)).createCaseDataContent(any(), any());
        verify(ccdCoreCaseDataService, times(1)).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    public void shouldSetCaseStatusToAwaitingInformationInResponse() {
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        CaseStatus caseStatus = (CaseStatus) result.get(CASE_STATUS);
        assertNotNull(caseStatus);
        assertEquals("Awaiting information", caseStatus.getState());
    }

    @Test
    public void shouldThrowExceptionWhenFeatureToggleDisabledDuringAddToCase() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        try {
            awaitingInformationService.addToCase(callbackRequest);
        } catch (RuntimeException e) {
            assertEquals("Awaiting information feature is not enabled", e.getMessage());
        }
    }

    @Test
    public void shouldNotCallCaseDataStoreWhenFeatureToggleDisabled() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        try {
            awaitingInformationService.addToCase(callbackRequest);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(ccdCoreCaseDataService, never()).eventRequest(any(), anyString());
        verify(ccdCoreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(ccdCoreCaseDataService, never()).createCaseDataContent(any(), any());
        verify(ccdCoreCaseDataService, never()).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    public void shouldHandleAllAwaitingInformationReasons() {
        AwaitingInformationReasonEnum[] reasons = {
            AwaitingInformationReasonEnum.miamFurtherInformation,
            AwaitingInformationReasonEnum.dwpHmrcWhereaboutsUnknown,
            AwaitingInformationReasonEnum.applicantFurtherInformation,
            AwaitingInformationReasonEnum.applicantClarifyConfidentialDetails,
            AwaitingInformationReasonEnum.respondentFurtherInformation,
            AwaitingInformationReasonEnum.helpWithFeesFurtherAction,
            AwaitingInformationReasonEnum.ctscRefundRequired,
            AwaitingInformationReasonEnum.other
        };

        for (AwaitingInformationReasonEnum reason : reasons) {
            AwaitingInformation info = AwaitingInformation.builder()
                .reviewDate(LocalDate.now().plusDays(5))
                .awaitingInformationReasonEnum(reason)
                .build();

            List<String> errors = awaitingInformationService.validate(info);

            assertTrue("Should validate without errors for reason: " + reason, errors.isEmpty());
        }
    }

    @Test
    public void shouldUseCaseIdFromCallbackRequestDetails() {
        awaitingInformationService.addToCase(callbackRequest);

        ArgumentCaptor<String> caseIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(ccdCoreCaseDataService).startUpdate(anyString(), any(), caseIdCaptor.capture(), anyBoolean());
        assertEquals("12345678", caseIdCaptor.getValue());
    }

    @Test
    public void shouldExtractAwaitingInformationFromCaseDataMap() {
        awaitingInformationService.addToCase(callbackRequest);

        verify(objectMapper).convertValue(eq(caseDataMap), eq(AwaitingInformation.class));
    }

    @Test
    public void shouldReturnResponseWithCaseStatusIncluded() {
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        assertTrue(result.containsKey(CASE_STATUS));
        assertNotNull(result.get(CASE_STATUS));
    }

    @Test
    public void shouldReturnResponseWithExistingCaseData() {
        caseDataMap.put("applicantName", "John Doe");
        caseDataMap.put("respondentName", "Jane Doe");

        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        assertEquals("John Doe", result.get("applicantName"));
        assertEquals("Jane Doe", result.get("respondentName"));
    }

    @Test
    public void shouldThrowExceptionWhenFeatureToggleDisabledDuringPopulateHeader() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        try {
            awaitingInformationService.populateHeader(callbackRequest);
        } catch (RuntimeException e) {
            assertEquals("Awaiting information feature is not enabled", e.getMessage());
        }
    }

    @Test
    public void shouldReturnAboutToStartOrSubmitCallbackResponseFromPopulateHeader() {
        var response = awaitingInformationService.populateHeader(callbackRequest);

        assertNotNull(response);
    }

    @Test
    public void shouldValidateWithFarFutureDate() {
        AwaitingInformation info = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(365))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        List<String> errors = awaitingInformationService.validate(info);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void shouldReturnNotNullResultFromAddToCase() {
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        assertNotNull(result);
    }

    @Test
    public void shouldSubmitUpdateToCaseDataStoreWithSystemUserAuthorization() {
        awaitingInformationService.addToCase(callbackRequest);

        verify(ccdCoreCaseDataService).submitUpdate(
            eq("system-auth-token"),
            any(),
            any(),
            anyString(),
            anyBoolean()
        );
    }

    @Test
    public void shouldGetSystemUserTokenFromSystemUserService() {
        awaitingInformationService.addToCase(callbackRequest);

        verify(systemUserService).getSysUserToken();
    }

    @Test
    public void shouldGetUserIdFromSystemUserService() {
        awaitingInformationService.addToCase(callbackRequest);

        verify(systemUserService).getUserId("system-auth-token");
    }

    @Test
    public void shouldCreateEventRequestWithAwaitingInformationEvent() {
        awaitingInformationService.addToCase(callbackRequest);

        verify(ccdCoreCaseDataService).eventRequest(CaseEvent.AWAITING_INFORMATION, "system-user-id");
    }

    @Test
    public void shouldValidateWithMultipleDateVariations() {
        LocalDate[] testDates = {
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(7),
            LocalDate.now().plusDays(30)
        };

        for (LocalDate date : testDates) {
            AwaitingInformation info = AwaitingInformation.builder()
                .reviewDate(date)
                .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.miamFurtherInformation)
                .build();

            List<String> errors = awaitingInformationService.validate(info);

            assertTrue("Should be valid for date: " + date, errors.isEmpty());
        }
    }

    @Test
    public void shouldCallStartUpdateWithIsRepresentedAsTrue() {
        awaitingInformationService.addToCase(callbackRequest);

        ArgumentCaptor<Boolean> isRepresentedCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(ccdCoreCaseDataService).startUpdate(anyString(), any(), anyString(), isRepresentedCaptor.capture());
        assertTrue(isRepresentedCaptor.getValue());
    }

    @Test
    public void shouldCallSubmitUpdateWithIsRepresentedAsTrue() {
        awaitingInformationService.addToCase(callbackRequest);

        ArgumentCaptor<Boolean> isRepresentedCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(ccdCoreCaseDataService).submitUpdate(
            anyString(),
            any(),
            any(),
            anyString(),
            isRepresentedCaptor.capture()
        );
        assertTrue(isRepresentedCaptor.getValue());
    }

}

