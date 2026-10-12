package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

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

    @ParameterizedTest
    @MethodSource("getLoggedInUserRoleWithRoleAssignmentService")
    void getLoggedInUserRoleWithRoleAssignmentService(String amRoleName, Roles idamRole, UserRoles expectedUserRole) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(amRoleName);
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .id("123")
                                                                    .roles(List.of(idamRole.getValue()))
                                                                    .build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(expectedUserRole, userRoleService.getLoggedInUserRole("test"));
    }

    static Stream<Arguments> getLoggedInUserRoleWithRoleAssignmentService() {
        Stream<Arguments> judgeRoles = InternalCaseworkerAmRolesEnum.JUDGE.getRoles().stream()
            .map(roleName -> Arguments.of(roleName, Roles.JUDGE, UserRoles.JUDGE));
        Stream<Arguments> legalAdviserRoles = InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles().stream()
            .map(roleName -> Arguments.of(roleName, Roles.LEGAL_ADVISER, UserRoles.LEGAL_ADVISER));
        Stream<Arguments> courtAdmin = InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles().stream()
            .map(roleName -> Arguments.of(roleName, Roles.COURT_ADMIN, UserRoles.COURT_ADMIN));
        Stream<Arguments> citizenRoles = Stream.of(Arguments.of("citizen", Roles.CITIZEN, UserRoles.CITIZEN));
        Stream<Arguments> solicitorRoles = Stream.of(Arguments.of("misc", Roles.SOLICITOR, UserRoles.SOLICITOR));
        Stream<Arguments> systemUpdateRoles = Stream.of(Arguments.of("caseworker-privatelaw-systemupdate",
                                                                     Roles.SYSTEM_UPDATE, UserRoles.SYSTEM_UPDATE));

        return Stream.of(judgeRoles, legalAdviserRoles, courtAdmin, citizenRoles, solicitorRoles, systemUpdateRoles)
            .flatMap(java.util.function.Function.identity());
    }

    @Test
    void testGetLoggedInUserTypeJudge() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .roles(List.of(Roles.JUDGE.getValue())).build());
        assertEquals(UserRoles.JUDGE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeLegalAdviser() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        assertEquals(UserRoles.JUDGE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeCourtAdmin() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .roles(List.of(Roles.COURT_ADMIN.getValue())).build());
        assertEquals(UserRoles.COURT_ADMIN.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeSolicitor() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SOLICITOR.getValue())).build());
        assertEquals(UserRoles.SOLICITOR.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeCitizen() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.CITIZEN.getValue())).build());
        assertEquals(UserRoles.CITIZEN.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeSystemUpdate() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeUnknownFromIdam() {
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .roles(Collections.emptyList()).build());
        assertEquals("", userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeCourtAdminFromAmRoleAssignment() {
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
    void testGetLoggedInUserTypeSolicitorFromIdam() {
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
    void testGetLoggedInUserTypeJudgeFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("allocated-magistrate");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
            .roles(List.of(Roles.JUDGE.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals(UserRoles.JUDGE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeLegalAdviserFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("tribunal-caseworker");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .id("123")
                                                                    .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals(UserRoles.JUDGE.name(), userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeUnknownFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("unknown");
        when(idamClient.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .id("123")
                                                                    .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals("", userRoleService.getLoggedInUserType("test"));
    }

    @Test
    void testGetLoggedInUserTypeForSystemUpdateFromIdam() {
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
    void testGetLoggedInUserTypeForCitizenFromIdam() {
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
