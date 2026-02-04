package uk.gov.hmcts.reform.prl.clients.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class LaCaseAssignmentService {
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



    public void validateSocialWorkerOrgRelationship(CaseData caseData,
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

    public void validateSocialWorkerAddRequest(CaseData caseData,
                                               Optional<String> socialWorkerRole,
                                               LocalAuthoritySocialWorker localAuthoritySocialWorker,
                                               List<String> errorList) {

        socialWorkerRole.ifPresentOrElse(
            role -> {
                validateSocialWorkerOrgRelationship(caseData, localAuthoritySocialWorker, errorList);
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

    public void validateSocialWorkerRemoveRequest(CaseData caseData,
                                                  LocalAuthoritySocialWorker localAuthoritySocialWorker,
                                                  List<String> errorList) {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .filter(roleAssignmentResponse ->
                        roleAssignmentResponse.getRoleName().equals(LOCAL_AUTHORITY_SOCIAL_WORKER)
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
