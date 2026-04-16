package uk.gov.hmcts.reform.prl.services.localauthority;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveLocalAuthoritySolicitorService {

    private final CaseAssignmentApi caseAssignmentApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator tokenGenerator;
    private final RoleAssignmentService roleAssignmentService;

    public void removeLocalAuthoritySolicitor(CaseData caseData) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        Set<String> solicitors = roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .filter(roleAssignment -> roleAssignment.getRoleName().equals(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE))
            .map(RoleAssignmentResponse::getActorId)
            .collect(Collectors.toSet());

        if (!solicitors.isEmpty()) {
            log.info("Removing local authority solicitors {} for case id {}", solicitors, caseData.getId());
            removeAmSolicitorCaseRole(caseData, solicitors);
        } else {
            log.info("No roles to remove for local authority solicitors for case id {}", caseData.getId());
        }
    }

    private void removeAmSolicitorCaseRole(CaseData caseData, Set<String> userIds) {
        try {
            log.info(
                "On case id {}, about to start remove case access {} for users {}",
                caseData.getId(),
                LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE,
                userIds
            );
            CaseAssignmentUserRolesRequest removeCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                caseData.getLocalAuthoritySolicitorOrganisationPolicy().getOrganisation().getOrganisationID(),
                userIds
            );

            caseAssignmentApi.removeCaseUserRoles(
                systemUserService.getSysUserToken(),
                tokenGenerator.generate(),
                removeCaseAssignedUserRolesRequest
            );
        } catch (FeignException ex) {
            String message = String.format(
                "Could not remove the user(s) %s role %s from the case %s",
                userIds,
                LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE,
                caseData.getId()
            );
            log.error(message, ex);
            throw new GrantCaseAccessException(message);
        }
    }


    private CaseAssignmentUserRolesRequest buildCaseAssignedUserRequest(Long caseId,
                                                                        String orgId,
                                                                        Set<String> users) {
        return users.stream()
            .map(user -> CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(caseId.toString())
                .organisationId(orgId)
                .userId(user)
                .caseRole(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE)
                .build())
            .collect(collectingAndThen(
                toList(),
                list ->
                    CaseAssignmentUserRolesRequest.builder()
                        .caseAssignmentUserRolesWithOrganisation(list)
                        .build()
            ));

    }
}
