package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
public class EmailControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @InjectMocks
    private EmailController emailController;


    @Test
    public void testSendSolicitorEmail() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(null)
            .state(null)
            .build();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest
                                                    .builder()
                                                    .caseDetails(caseDetails)
                                                    .build();

        UserDetails userDetails = UserDetails.builder().build();

        when(userService.getUserDetails("Authorisation")).thenReturn(userDetails);

        emailController.sendSolicitorEmail(callbackRequest, "Authorisation");

        //verify(solicitorEmailService).sendEmail(caseDetails, userDetails);

    }

    @Test
    public void testSendCaseWorkerEmail() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(null)
            .state(null)
            .build();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model.CallbackRequest
            .builder()
            .caseDetails(caseDetails)
            .build();

        UserDetails userDetails = UserDetails.builder().build();

        when(userService.getUserDetails("Authorisation")).thenReturn(userDetails);

        emailController.sendSolicitorEmail(callbackRequest, "Authorisation");

        //verify(caseWorkerEmailService).sendEmail(caseDetails, userDetails);

    }

}
