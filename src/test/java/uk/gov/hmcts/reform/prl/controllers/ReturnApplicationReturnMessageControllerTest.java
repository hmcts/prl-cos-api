package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ReturnApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;


@RunWith(SpringRunner.class)
public class ReturnApplicationReturnMessageControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ReturnApplicationReturnMessageController returnApplicationReturnMessageController;

    @Mock
    private UserService userService;

    @Mock
    private ReturnApplicationService returnApplicationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private EventService eventPublisher;

    @Mock
    private AllTabServiceImpl allTabsService;

    private CallbackRequest callbackRequest;

    CaseData casedata;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();

        casedata = CaseData.builder()
            .applicantCaseName("TestCase")
            .id(123L)
            .applicants(applicantList)
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();
    }

    @Test
    public void shouldStartReturnApplicationReturnMessageWithCaseDetails() throws Exception {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("returnMessage", "Test");

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class)).thenReturn(casedata);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = returnApplicationReturnMessageController
            .returnApplicationReturnMessage(
            authToken,
            callbackRequest);
        verify(userService).getUserDetails(authToken);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void testReturnApplicationEmailNotification() throws Exception {

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder()
            .applicants(applicantList)
            .applicantSolicitorEmailAddress("testing@test.com")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(stringObjectMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();

        doNothing().when(caseWorkerEmailService).sendReturnApplicationEmailToSolicitor(callbackRequest.getCaseDetails());

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
            returnApplicationReturnMessageController.returnApplicationEmailNotification(callbackRequest);

        verify(allTabsService, times(1)).getAllTabsFields(any(CaseData.class));
    }
}
