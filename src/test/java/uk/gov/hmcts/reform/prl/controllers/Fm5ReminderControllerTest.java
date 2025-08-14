package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Fm5ReminderControllerTest {

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @InjectMocks
    private Fm5ReminderController fm5ReminderController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private Fm5ReminderService fm5ReminderService;


    @Test
    public void test_Fm5ReminderNotification() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        fm5ReminderController.sendFm5ReminderNotifications(18L, AUTH_TOKEN, S2S_TOKEN);
        verify(fm5ReminderService, times(1)).sendFm5ReminderNotifications(18L);
    }

    @Test(expected = RuntimeException.class)
    public void test_Fm5ReminderNotificationThrowsException() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        fm5ReminderController.sendFm5ReminderNotifications(18L, AUTH_TOKEN, S2S_TOKEN);
        verifyNoInteractions(fm5ReminderService);
    }

    @Test
    public void test_FetchFm5ReminderEligibleCases() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseDetailsList.add(CaseDetails.builder().build());
        when(fm5ReminderService.retrieveCasesInHearingStatePendingFm5Reminders()).thenReturn(caseDetailsList);
        when(fm5ReminderService.getQualifiedCasesAndHearingsForNotifications(
            any(List.class),
            any(Long.class))).thenReturn(new HashMap<>());
        fm5ReminderController.fetchFm5ReminderEligibleCases(18L, AUTH_TOKEN, S2S_TOKEN);
        verify(fm5ReminderService, times(1)).retrieveCasesInHearingStatePendingFm5Reminders();
    }

    @Test
    public void test_FetchFm5ReminderEligibleCasesWhenCaseDetailsEmpty() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(true);
        when(fm5ReminderService.retrieveCasesInHearingStatePendingFm5Reminders()).thenReturn(null);
        ResponseEntity response = fm5ReminderController.fetchFm5ReminderEligibleCases(18L, AUTH_TOKEN, S2S_TOKEN);
        assertNotNull(response);
        assertEquals("No Cases Eligible for FM5 notification", response.getBody());
    }

    @Test
    public void test_FetchFm5ReminderEligibleCasesWhenNoAuthorization() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);
        assertExpectedException(
            () -> {
                fm5ReminderController.fetchFm5ReminderEligibleCases(18L, AUTH_TOKEN, S2S_TOKEN);
            }, RuntimeException.class, "Invalid Client"
        );

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}
