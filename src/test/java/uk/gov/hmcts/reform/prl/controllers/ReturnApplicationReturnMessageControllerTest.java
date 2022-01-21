package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;


@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class ReturnApplicationReturnMessageControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ReturnApplicationReturnMessageController returnApplicationReturnMessageController;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CaseDetails caseDetails;


    public static final String authToken = "Bearer TestAuthToken";

    CaseData casedata;

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .forename("Testing")
            .surname("CTSC")
            .build();

    }


    @Test
    public void testUserDetailsForCaseWorkerName() throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        returnApplicationReturnMessageController.returnApplicationReturnMessage(authToken, callbackRequest);

        verify(userService).getUserDetails(authToken);
        verifyNoMoreInteractions(userService);

    }

    @Test
    public void whenNoOptionSelectedThenNoRejectReasonSelectedReturnTrue() {
        casedata = CaseData.builder().build();

        assert returnApplicationReturnMessageController.noRejectReasonSelected(casedata);
    }

    @Test
    public void whenHasOptionSelectedThenNoRejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        assert !returnApplicationReturnMessageController.noRejectReasonSelected(casedata);
    }

}
