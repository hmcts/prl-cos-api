package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LoggedInUserTypeServiceTest {

    @InjectMocks
    LoggedInUserService loggedInUserService;
    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentApi roleAssignmentApi;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Test
    public void testGetLoggedInUserTypeSolicitor() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SOLICITOR.getValue())).build());
        assertEquals(UserRoles.SOLICITOR.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeCitizen() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.CITIZEN.getValue())).build());
        assertEquals(UserRoles.CITIZEN.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSystemUpdate() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), loggedInUserService.getLoggedInUserType("test"));
    }


    @Test
    public void testGetLoggedInUserTypeCourtAdminFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "hearing-centre-admin");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.COURT_ADMIN.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSolicitorFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-solicitor");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.SOLICITOR.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SOLICITOR.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeJudgeFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("allocated-magistrate");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals(UserRoles.JUDGE.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForSystemUpdateFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-systemupdate");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), loggedInUserService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForCitizenFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("citizen");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.CITIZEN.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.CITIZEN.name(), loggedInUserService.getLoggedInUserType("test"));
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
