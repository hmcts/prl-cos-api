package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
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

@RunWith(MockitoJUnitRunner.class)
public class UserRoleServiceTest {

    @InjectMocks
    private UserRoleService userRoleService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private RoleAssignmentApi roleAssignmentApi;

    @Mock
    private IdamClient idamClient;


    @Test
    public void testGetLoggedInUserTypeSolicitor() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SOLICITOR.getValue())).build());
        assertEquals(UserRoles.SOLICITOR.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeCitizen() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.CITIZEN.getValue())).build());
        assertEquals(UserRoles.CITIZEN.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSystemUpdate() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), userRoleService.getLoggedInUserType("test"));
    }


    @Test
    public void testGetLoggedInUserTypeCourtAdminFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "hearing-centre-admin");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.COURT_ADMIN.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSolicitorFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-solicitor");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.SOLICITOR.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SOLICITOR.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeJudgeFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("allocated-magistrate");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals(UserRoles.JUDGE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForSystemUpdateFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-systemupdate");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForCitizenFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("citizen");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.CITIZEN.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.CITIZEN.name(), userRoleService.getLoggedInUserType("test"));
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
