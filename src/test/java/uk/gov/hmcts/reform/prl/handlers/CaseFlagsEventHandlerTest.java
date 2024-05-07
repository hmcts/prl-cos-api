package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsEventHandlerTest {

    public static final String TEST_AUTH = "test-auth";
    @Mock
    private UserService userService;
    @Mock
    private RoleAssignmentApi roleAssignmentApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    private CaseFlagsEventHandler caseFlagsEventHandler;

    @Test
    public void testTriggerDummyEventForCaseFlags() {
        List<RoleAssignmentResponse> roleAssignmentResponses =  new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse =  new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("ctsc");
        roleAssignmentResponses.add(roleAssignmentResponse);
        Mockito.when(authTokenGenerator.generate()).thenReturn("service-token");
        Mockito.when(roleAssignmentApi
                         .getRoleAssignments(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any()))
                .thenReturn(RoleAssignmentServiceResponse.builder()
                                .roleAssignmentResponse(roleAssignmentResponses)
                                .build());
        Mockito.when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = new StartAllTabsUpdateDataContent(TEST_AUTH, EventRequestData.builder().build(),
                                                StartEventResponse.builder().build(), caseDataMap, caseData, null);
        Mockito.when(allTabService
                         .getStartUpdateForSpecificEvent(Mockito.any(),Mockito.any())).thenReturn(
            startAllTabsUpdateDataContent);
        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(CallbackRequest.builder()
                                                               .caseDetails(CaseDetails.builder()
                                                                                .id(123L).build())
                                                               .build(), TEST_AUTH);
        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        Mockito.verify(allTabService,
                       Mockito.times(1)).getStartUpdateForSpecificEvent(Mockito.any(),Mockito.any());
        Mockito.verify(allTabService,Mockito.times(1)).submitAllTabsUpdate(
            Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyMap());
    }
}
