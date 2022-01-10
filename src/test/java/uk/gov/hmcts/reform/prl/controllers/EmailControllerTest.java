package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class EmailControllerTest {

    @Mock
    UserService userService;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @InjectMocks
    private EmailController emailController;


    @Test
    public void testSendEmail() {
        CaseDetails caseDetails = CaseDetails.builder()
            .build();

        when(userService.getUserDetails("Authorisation")).thenReturn(UserDetails.builder().build());

        emailController.sendSolicitorEmail(CallbackRequest.builder().build(), "Authorisation");

        verify(solicitorEmailService, times(1));

    }

}
