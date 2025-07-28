package uk.gov.hmcts.reform.prl.clients.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryAttributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.utils.EmailUtils.maskEmail;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class CcdCaseAssignmentService {
    private final CaseAssignmentApi caseAssignmentApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator tokenGenerator;
    private final OrganisationService organisationService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final ObjectMapper objectMapper;

    public void grantCaseAccess(final CaseData caseData,
                                final String userId,
                                final String caseRole) {
        Set<String> userIds = Set.of(userId);
        try {
            log.info("About to start granting {} case access for users {}", caseRole, userIds);
            AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                caseRole,
                allocatedBarrister.getBarristerOrg().getOrganisationID(),
                userIds
            );

            caseAssignmentApi.addCaseUserRoles(
                systemUserService.getSysUserToken(),
                tokenGenerator.generate(),
                addCaseAssignedUserRolesRequest
            );
        } catch (FeignException ex) {
            String message = String.format("User(s) %s not granted %s to case %s", userIds, caseRole, caseData.getId());
            log.error(message, ex);
            throw new GrantCaseAccessException(message);
        }
    }

    private CaseAssignmentUserRolesRequest buildCaseAssignedUserRequest(Long caseId,
                                                                        String caseRole,
                                                                        String orgId,
                                                                        Set<String> users) {
        return users.stream()
            .map(user -> CaseAssignmentUserRoleWithOrganisation.builder()
                .caseDataId(caseId.toString())
                .organisationId(orgId)
                .userId(user)
                .caseRole(caseRole)
                .build())
            .collect(collectingAndThen(
                toList(),
                list ->
                    CaseAssignmentUserRolesRequest.builder()
                        .caseAssignmentUserRolesWithOrganisation(list)
                        .build()
            ));

    }

    public void removeBarrister(final CaseData caseData,
                                final String userId,
                                final String caseRole) {
        Set<String> userIds = Set.of(userId);
        try {
            log.info("About to start remove case access {} for users {}", caseRole, userIds);
            AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                caseRole,
                allocatedBarrister.getBarristerOrg().getOrganisationID(),
                userIds
            );

            caseAssignmentApi.removeCaseUserRoles(
                systemUserService.getSysUserToken(),
                tokenGenerator.generate(),
                addCaseAssignedUserRolesRequest
            );
        } catch (FeignException ex) {
            String message = String.format(
                "Could not remove the user(s) %s role %s from the case %s",
                userIds,
                caseRole,
                caseData.getId()
            );
            log.error(message, ex);
            throw new GrantCaseAccessException(message);
        }
    }

    public void validateBarristerOrgRelationship(AllocatedBarrister allocatedBarrister,
                                                 List<String> errorList) {
        OrgSolicitors organisationSolicitorDetails = organisationService.getOrganisationSolicitorDetails(
            systemUserService.getSysUserToken(),
            allocatedBarrister.getBarristerOrg().getOrganisationID()
        );

        organisationSolicitorDetails.getUsers().stream()
            .filter(user -> user.getEmail().equals(allocatedBarrister.getBarristerEmail()))
            .findAny()
            .ifPresentOrElse(
                user -> { },
                () -> {
                    log.error("Barrister {} is not associated with the organisation {}",
                              maskEmail(allocatedBarrister.getBarristerEmail()),
                              allocatedBarrister.getBarristerOrg().getOrganisationID());

                    errorList.add("Barrister doesn't belong to selected organisation");
                });
    }

    public void validateUserRole(CaseData caseData,
                                 String userId,
                                 String userRole,
                                 List<String> errorList) {

        CaseAssignmentUserRolesResource userRoles = caseAssignmentApi.getUserRoles(
            systemUserService.getSysUserToken(),
            tokenGenerator.generate(),
            String.valueOf(caseData.getId()),
            userId
        );

        userRoles.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .filter(not(caseRole -> caseRole.equals(userRole)))
            .findAny()
            .ifPresentOrElse(caseRole -> { },
                             () -> {
                                 log.error("Barrister {} is not associated with the case {}",
                                           maskEmail(caseData.getAllocatedBarrister().getBarristerEmail()),
                                           caseData.getId());
                                 errorList.add(String.format("Barrister %s is not associated with the case",
                                                             caseData.getAllocatedBarrister().getBarristerEmail()));

                             });
    }

    public void validateCaseRoles(CaseData caseData,
                                  String userRole,
                                  List<String> errorList) {
        RoleAssignmentQueryRequest roleAssignmentQueryRequest = RoleAssignmentQueryRequest.builder()
            .attributes(QueryAttributes.builder()
                            .caseId(List.of(String.valueOf(caseData.getId())))
                            .build())
            .validAt(LocalDateTime.now())
            .build();
        String systemAuthorisation = systemUserService.getSysUserToken();

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.queryRoleAssignments(
            systemAuthorisation,
            tokenGenerator.generate(),
            null,
            roleAssignmentQueryRequest
        );

        CaseAssignmentUserRolesResource userRoles = caseAssignmentApi.getUserRoles(
            systemAuthorisation,
            tokenGenerator.generate(),
            String.valueOf(caseData.getId()),
            systemUserService.getUserId(systemAuthorisation)
        );

        userRoles.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .filter(caseRole -> caseRole.equals(userRole))
            .findAny()
            .ifPresent(caseRole -> {
                log.error(
                    "Case role {} is already associated with the case {}",
                    caseRole,
                    caseData.getId()
                );
                errorList.add("A barrister is already associated with the case");
            });
    }
}
