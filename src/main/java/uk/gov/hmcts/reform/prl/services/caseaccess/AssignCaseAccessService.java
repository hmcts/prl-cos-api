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

            log.info("CaseId: {} of type {} assigning case access to user {}", caseId, CASE_TYPE, userId);

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

            log.info("CaseId: {} assigned case access to user {}", caseId, userId);
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


}
