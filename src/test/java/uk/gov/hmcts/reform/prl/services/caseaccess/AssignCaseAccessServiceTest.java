package uk.gov.hmcts.reform.prl.services.caseaccess;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;

@RunWith(MockitoJUnitRunner.class)
public class AssignCaseAccessServiceTest {

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @InjectMocks
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private UserService userService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private RoleAssignmentApi roleAssignmentApi;



    @Test
    public void testAssignCaseAccess() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-solicitor");
        UserDetails userDetails
            = new UserDetails("test-id","test@test.com", "test", "test", roles);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(ccdDataStoreService).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        when(authTokenGenerator.generate()).thenReturn("Generate");
        doNothing().when(assignCaseAccessClient)
            .assignCaseAccess(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verify(userService, times(1)).getUserDetails(Mockito.anyString());
        verify(ccdDataStoreService, times(1)).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        verify(authTokenGenerator, times(1)).generate();
        verify(assignCaseAccessClient, times(1)).assignCaseAccess(Mockito.anyString(), Mockito.anyString(),
                                                        Mockito.anyBoolean(), Mockito.any());
    }

    @Test
    public void testAssignCaseAccess2() {

        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(false);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(ccdDataStoreService);
        verifyNoMoreInteractions(assignCaseAccessClient);
    }

    @Test
    public void testAssignCaseAccessForCourtAdmin() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-courtadmin");
        UserDetails userDetails
            = new UserDetails("test-id","test@test.com", "test", "test", roles);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(ccdDataStoreService).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verify(userService, times(1)).getUserDetails(Mockito.anyString());
        verify(ccdDataStoreService, times(1)).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testAssignCaseAccessForCourtAdminSec() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-courtadmin");
        UserDetails userDetails
            = new UserDetails("test-id","test@test.com", "test", "test", roles);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("citizen");

        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(ccdDataStoreService).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        when(launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)).thenReturn(true);
        when(roleAssignmentApi.getRoleAssignments("ABC123", authTokenGenerator.generate(),
                                                  null, "test-id")).thenReturn(roleAssignmentServiceResponse);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verify(userService, times(1)).getUserDetails(Mockito.anyString());
        verify(ccdDataStoreService, times(1)).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
    }

    private RoleAssignmentServiceResponse setAndGetRoleAssignmentServiceResponse(String roleName) {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(roleName);
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);
        return roleAssignmentServiceResponse;
    }
}

