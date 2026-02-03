package uk.gov.hmcts.reform.prl.clients.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.localauthority.LocalAuthoritySocialWorker;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.List;
import java.util.Optional;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class LACaseAssignmentService {
    public static final String LOCAL_AUTHORITY_SOCIAL_WORKER = "[LASOCIALWORKER]";
    private final CaseAssignmentApi caseAssignmentApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator tokenGenerator;
    private final OrganisationService organisationService;
    private final RoleAssignmentService roleAssignmentService;
    private final MaskEmail maskEmail;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;
    private final BarristerHelper barristerHelper;
    private final BarristerRemoveService barristerRemoveService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public void addLocalAuthoritySocialWorker(CaseData caseData,
                                              String userId,
                                              String socialWorkerRole,
                                              LocalAuthoritySocialWorker localAuthoritySocialWorker) {
        log.info(
            "On case id {}, about to add {} case access for users {}",
            caseData.getId(),
            socialWorkerRole,
            userId
        );
        grantUserCaseAccess(caseData, userId, socialWorkerRole, localAuthoritySocialWorker);
    }

    private void grantUserCaseAccess(final CaseData caseData,
                                     final String userId,
                                     final String caseRole,
                                     LocalAuthoritySocialWorker localAuthoritySocialWorker) {
        try {
            String organisationID = localAuthoritySocialWorker.getLaSocialWorkerOrg().getOrganisationID();
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                caseRole,
                organisationID,
                userId
            );

            caseAssignmentApi.addCaseUserRoles(
                systemUserService.getSysUserToken(),
                tokenGenerator.generate(),
                addCaseAssignedUserRolesRequest
            );
        } catch (FeignException ex) {
            String message = String.format("User %s not granted %s to case %s", userId, caseRole, caseData.getId());
            log.error(message, ex);
            throw new GrantCaseAccessException(message);
        }
    }

    private CaseAssignmentUserRolesRequest buildCaseAssignedUserRequest(Long caseId,
                                                                        String caseRole,
                                                                        String orgId,
                                                                        String user) {
        return CaseAssignmentUserRolesRequest.builder().caseAssignmentUserRolesWithOrganisation(
            List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                        .caseDataId(caseId.toString())
                        .organisationId(orgId)
                        .userId(user)
                        .caseRole(caseRole)
                        .build())).build();

    }

    public void removeLASocialWorker(final CaseData caseData, LocalAuthoritySocialWorker localAuthoritySocialWorker) {
        String userId = localAuthoritySocialWorker.getUserId();
        try {
            log.info(
                "On case id {}, about to start remove case access {} for users {}",
                caseData.getId(),
                LOCAL_AUTHORITY_SOCIAL_WORKER,
                userId
            );
            CaseAssignmentUserRolesRequest removeCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                LOCAL_AUTHORITY_SOCIAL_WORKER,
                localAuthoritySocialWorker.getLaSocialWorkerOrg().getOrganisationID(),
                userId
            );

            caseAssignmentApi.removeCaseUserRoles(
                systemUserService.getSysUserToken(),
                tokenGenerator.generate(),
                removeCaseAssignedUserRolesRequest
            );
        } catch (FeignException ex) {
            String message = String.format(
                "Could not remove the user %s role %s from the case %s",
                userId,
                LOCAL_AUTHORITY_SOCIAL_WORKER,
                caseData.getId()
            );
            log.error(message, ex);
            throw new GrantCaseAccessException(message);
        }
    }

    public void validateBarristerOrgRelationship(CaseData caseData,
                                                 LocalAuthoritySocialWorker localAuthoritySocialWorker,
                                                 List<String> errorList) {
        OrgSolicitors organisationSolicitorDetails = organisationService.getOrganisationSolicitorDetails(
            systemUserService.getSysUserToken(),
            localAuthoritySocialWorker.getLaSocialWorkerOrg().getOrganisationID()
        );

        organisationSolicitorDetails.getUsers().stream()
            .filter(user -> user.getEmail().equals(localAuthoritySocialWorker.getLaSocialWorkerEmail()))
            .findAny()
            .ifPresentOrElse(
                user -> {
                },
                () -> {
                    log.error(
                        "Case id {}:"
                            + " Local authority Social worker {} is not registered with the selected organisation {}",
                        caseData.getId(),
                        maskEmail.mask(localAuthoritySocialWorker.getLaSocialWorkerEmail()),
                        localAuthoritySocialWorker.getLaSocialWorkerOrg().getOrganisationID()
                    );

                    errorList.add("Local authority Social worker is not registered with the selected organisation");
                }
            );
    }

    public void validateCaseRoles(CaseData caseData,
                                  String userRole,
                                  List<String> errorList) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .map(RoleAssignmentResponse::getRoleName)
            .filter(userRole::equals)
            .findAny()
            .ifPresent(caseRole -> {
                log.error(
                    "Case role {} is already associated with the case {}",
                    caseRole,
                    caseData.getId()
                );
                errorList.add("A Local authority social worker is already associated with the case");
            });
    }

    public void validateAddRequest(CaseData caseData,
                                   Optional<String> socialWorkerRole,
                                   LocalAuthoritySocialWorker localAuthoritySocialWorker,
                                   List<String> errorList) {

        socialWorkerRole.ifPresentOrElse(
            role -> {
                validateBarristerOrgRelationship(caseData, localAuthoritySocialWorker, errorList);
                validateCaseRoles(caseData, role, errorList);
            },
            () -> {
                errorList.add("Could not map to barrister case role");
                log.error(
                    "Case id {}, could not map to LA social worker case role for selected party {}",
                    caseData.getId(),
                    localAuthoritySocialWorker.getUserId()
                );
            }
        );
    }

    public void validateRemoveRequest(CaseData caseData,
                                      LocalAuthoritySocialWorker localAuthoritySocialWorker,
                                      List<String> errorList) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .filter(roleAssignmentResponse ->
                        roleAssignmentResponse.getRoleName().equals("[LASOCIALWORKER]")
                            && roleAssignmentResponse.getActorId().equals(localAuthoritySocialWorker.getUserId()))
            .findAny()
            .ifPresentOrElse(
                roleName -> {
                },
                () -> {
                    log.error(
                        "LA Social worker {} is not associated with the case {}",
                        maskEmail.mask(localAuthoritySocialWorker.getLaSocialWorkerEmail()),
                        caseData.getId()
                    );
                    errorList.add("LA Social worker is not associated with the case");
                }
            );
    }
}
