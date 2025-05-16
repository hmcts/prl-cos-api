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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsEventHandlerTest {

    public static final String TEST_AUTH = "test-auth";
    public static final String SERVICE_TOKEN = "service-token";
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
        Mockito.when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        Mockito.when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());
        getRoleAssignmentsWith("ctsc");
        getCaseFlagsWithStatus("Requested");

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = new StartAllTabsUpdateDataContent(TEST_AUTH, EventRequestData.builder().build(),
                                                StartEventResponse.builder().build(), caseDataMap, caseData, null
        );
        Mockito.when(allTabService
                         .getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any())).thenReturn(
            startAllTabsUpdateDataContent);
        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(CallbackRequest.builder()
                                                               .caseDetails(CaseDetails.builder()
                                                                                .id(123L).build())
                                                               .build(), TEST_AUTH);
        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);
        Mockito.verify(
            allTabService,
            Mockito.times(1)
        ).getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any());
        Mockito.verify(allTabService, Mockito.times(1)).submitAllTabsUpdate(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testTriggerDummyEventForWhenNotCourtAdminOrTeamLeaderUser() {
        Mockito.when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        Mockito.when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());

        getRoleAssignmentsWith("admin");
        getCaseFlagsWithStatus("Requested");

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = new StartAllTabsUpdateDataContent(TEST_AUTH, EventRequestData.builder().build(),
                                                StartEventResponse.builder().build(), caseDataMap, caseData, null
        );
        Mockito.when(allTabService
                         .getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any())).thenReturn(
            startAllTabsUpdateDataContent);
        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(CallbackRequest.builder()
                                                               .caseDetails(CaseDetails.builder()
                                                                                .id(123L).build())
                                                               .build(), TEST_AUTH);
        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        Mockito.verify(
            allTabService,
            Mockito.times(0)
        ).getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any());
        Mockito.verify(allTabService, Mockito.times(0)).submitAllTabsUpdate(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyMap());
    }

    @Test
    public void testWaTaskIsNotCreatedWhenStatusIsActive() {
        Mockito.when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        Mockito.when(userService.getUserDetails(Mockito.any())).thenReturn(UserDetails.builder().build());

        getRoleAssignmentsWith("team-leader");
        getCaseFlagsWithStatus("Active");

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = new StartAllTabsUpdateDataContent(TEST_AUTH, EventRequestData.builder().build(),
                                                StartEventResponse.builder().build(), caseDataMap, caseData, null
        );
        Mockito.when(allTabService
                         .getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any())).thenReturn(
            startAllTabsUpdateDataContent);
        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(CallbackRequest.builder()
                                                               .caseDetails(CaseDetails.builder()
                                                                                .id(123L).build())
                                                               .build(), TEST_AUTH);
        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);

        Mockito.verify(
            allTabService,
            Mockito.times(0)
        ).getStartUpdateForSpecificEvent(Mockito.any(), Mockito.any());
        Mockito.verify(allTabService, Mockito.times(0)).submitAllTabsUpdate(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyMap());
    }

    private void getRoleAssignmentsWith(String roleName) {
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(roleName);
        List<RoleAssignmentResponse> roleAssignmentResponses = List.of(roleAssignmentResponse);

        Mockito.when(roleAssignmentApi.getRoleAssignments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(roleAssignmentResponses)
                            .build());
    }

    private void getCaseFlagsWithStatus(String status) {
        List<Element<FlagDetail>> flagDetails = List.of(
            new Element<>(UUID.randomUUID(), FlagDetail.builder()
                .status(status)
                .dateTimeCreated(LocalDateTime.now())
                .build())
        );

        Flags caseFlags = Flags.builder()
            .details(flagDetails)
            .build();

        CaseData mappedCaseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .state(State.CASE_ISSUED)
            .caseFlags(caseFlags)
            .build();

        Mockito.when(objectMapper.convertValue(Mockito.any(), Mockito.eq(CaseData.class)))
            .thenReturn(mappedCaseData);
    }
}
