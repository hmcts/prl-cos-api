package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRoleService {

    private final LaunchDarklyClient launchDarklyClient;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    public String getLoggedInUserType(String authorisation) {
        UserDetails userDetails = getUserDetails(authorisation);
        UserRoles loggedInUserType;
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            loggedInUserType = getUserRoleFromRoleAssignmentService(authorisation, userDetails);
        } else {
            loggedInUserType = getUserRoleFromIdam(userDetails);
        }

        return loggedInUserType != null ? loggedInUserType.name() : "";
    }

    private UserDetails getUserDetails(String authorisation) {
        return idamClient.getUserDetails(authorisation);
    }

    private UserRoles getUserRoleFromRoleAssignmentService(String authorisation, UserDetails userDetails) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
            authorisation,
            authTokenGenerator.generate(),
            null,
            userDetails.getId()
        );
        Set<String> roles = roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .map(RoleAssignmentResponse::getRoleName)
            .collect(Collectors.toSet());

        if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.JUDGE.getRoles()::contains)
            || roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles()::contains)) {
            return UserRoles.JUDGE;
        } else if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles()::contains)) {
            return UserRoles.COURT_ADMIN;
        } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            return UserRoles.SOLICITOR;
        } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
            return UserRoles.CITIZEN;
        } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
            return UserRoles.SYSTEM_UPDATE;
        } else {
            return null;
        }
    }

    private UserRoles getUserRoleFromIdam(UserDetails userDetails) {
        if (userDetails.getRoles().contains(Roles.JUDGE.getValue()) || userDetails.getRoles().contains(Roles.LEGAL_ADVISER.getValue())) {
            return UserRoles.JUDGE;
        } else if (userDetails.getRoles().contains(Roles.COURT_ADMIN.getValue())) {
            return UserRoles.COURT_ADMIN;
        } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            return UserRoles.SOLICITOR;
        } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
            return UserRoles.CITIZEN;
        } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
            return UserRoles.SYSTEM_UPDATE;
        } else {
            return null;
        }
    }
}
