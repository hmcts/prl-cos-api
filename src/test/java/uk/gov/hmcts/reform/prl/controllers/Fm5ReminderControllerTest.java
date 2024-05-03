package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Fm5ReminderControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    private MockMvc mockMvc;
    @InjectMocks
    private Fm5ReminderController fm5ReminderController;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private Fm5ReminderService fm5ReminderService;

    @Mock
    private IdamClient idamClient;


    @Test
    void test_Fm5ReminderNotification() {
        fm5ReminderController.sendFm5ReminderNotifications();
        verify(fm5ReminderService, times(1)).sendFm5ReminderNotifications();
    }

}
