package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoggedInUserService {

    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;
    private final UserService userService;

    public String getLoggedInUserType(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String loggedInUserType;
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            //This would check for roles from AM for Judge/Legal advisor/Court admin
            //if it doesn't find then it will check for idam roles for rest of the users
            RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                authorisation,
                authTokenGenerator.generate(),
                null,
                userDetails.getId()
            );
            List<String> roles = roleAssignmentServiceResponse.getRoleAssignmentResponse().stream().map(role -> role.getRoleName()).collect(
                Collectors.toList());
            if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.JUDGE.getRoles()::contains)
                || roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles()::contains)) {
                loggedInUserType = UserRoles.JUDGE.name();
            } else if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles()::contains)) {
                loggedInUserType = UserRoles.COURT_ADMIN.name();
            } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
                loggedInUserType = UserRoles.SOLICITOR.name();
            } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
                loggedInUserType = UserRoles.CITIZEN.name();
            } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
                loggedInUserType = UserRoles.SYSTEM_UPDATE.name();
            } else {
                loggedInUserType = "";
            }
        } else {
            if (userDetails.getRoles().contains(Roles.JUDGE.getValue()) || userDetails.getRoles().contains(Roles.LEGAL_ADVISER.getValue())) {
                loggedInUserType = UserRoles.JUDGE.name();
            } else if (userDetails.getRoles().contains(Roles.COURT_ADMIN.getValue())) {
                loggedInUserType = UserRoles.COURT_ADMIN.name();
            } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
                loggedInUserType = UserRoles.SOLICITOR.name();
            } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
                loggedInUserType = UserRoles.CITIZEN.name();
            } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
                loggedInUserType = UserRoles.SYSTEM_UPDATE.name();
            } else {
                loggedInUserType = "";
            }
        }

        return loggedInUserType;
    }
}
