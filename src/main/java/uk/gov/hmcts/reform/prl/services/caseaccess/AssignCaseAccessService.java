package uk.gov.hmcts.reform.prl.services.caseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.caseaccess.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;


@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessService {

    private final CcdDataStoreService ccdDataStoreService;
    private final AuthTokenGenerator authTokenGenerator;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final UserService userService;
    private final LaunchDarklyClient launchDarklyClient;

    private final RoleAssignmentApi roleAssignmentApi;
    private final SystemUserService systemUserService;


    public void assignCaseAccess(String caseId, String authorisation) {

        if (launchDarklyClient.isFeatureEnabled("share-a-case")) {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            List<String> roles = userDetails.getRoles();
            if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
                RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                    authorisation,
                    authTokenGenerator.generate(),
                    null,
                    userDetails.getId()
                );
                roles = CaseUtils.mapAmUserRolesToIdamRoles(roleAssignmentServiceResponse, authorisation, userDetails);
            }
            boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
            String userId = userDetails.getId();

            if (!isCourtStaff) {
                String serviceToken = authTokenGenerator.generate();
                assignCaseAccessClient.assignCaseAccess(
                    authorisation,
                    serviceToken,
                    true,
                    buildAssignCaseAccessRequest(caseId, userId, CASE_TYPE)
                );
            }
            ccdDataStoreService.removeCreatorRole(caseId, authorisation);
        }
    }

    private AssignCaseAccessRequest buildAssignCaseAccessRequest(String caseId, String userId, String caseTypeId) {

        return AssignCaseAccessRequest
            .builder()
            .caseId(caseId)
            .assigneeId(userId)
            .caseTypeId(caseTypeId)
            .build();
    }

    public void assignCaseAccessToUserWithRole(
        String caseId,
        String assigneeUserId,
        String caseRole
    ) {
        if (!launchDarklyClient.isFeatureEnabled("share-a-case")
        ) {
            log.info("share-a-case flag disabled; skipping access assignment");
            return;
        }

        String serviceToken = authTokenGenerator.generate();

        AssignCaseAccessRequest request = AssignCaseAccessRequest.builder()
            .caseId(caseId)
            .assigneeId(assigneeUserId)
            .caseTypeId(CASE_TYPE)
            .caseRoles(List.of(caseRole))       // e.g. "[C100RESPONDENTSOLICITOR1]"
            .build();

        log.info("Assigning case {} to user {} with role {}", caseId, assigneeUserId, caseRole);


        String sysUserToken = systemUserService.getSysUserToken();
        assignCaseAccessClient.assignCaseAccess(
            sysUserToken,  // sysuser
            serviceToken,
            false,
            request
        );
    }


}
