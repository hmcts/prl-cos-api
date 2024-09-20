package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Fm5ReminderControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @InjectMocks
    private Fm5ReminderController fm5ReminderController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private Fm5ReminderService fm5ReminderService;


    @Test
    public void test_Fm5ReminderNotification() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        fm5ReminderController.sendFm5ReminderNotifications(18L, authToken, s2sToken);
        verify(fm5ReminderService, times(1)).sendFm5ReminderNotifications(18L);
    }

    @Test(expected = RuntimeException.class)
    public void test_Fm5ReminderNotificationThrowsException() {
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        fm5ReminderController.sendFm5ReminderNotifications(18L, authToken, s2sToken);
        verifyNoInteractions(fm5ReminderService);
    }

}
