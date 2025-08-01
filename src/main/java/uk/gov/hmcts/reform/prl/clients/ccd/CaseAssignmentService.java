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
import uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.EmailUtils.maskEmail;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class CaseAssignmentService {
    private final CaseAssignmentApi caseAssignmentApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator tokenGenerator;
    private final OrganisationService organisationService;
    private final RoleAssignmentService roleAssignmentService;
    private final ObjectMapper objectMapper;

    public void grantBarristerCaseAccess(final CaseData caseData,
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

    public void removeCaseAccess(final CaseData caseData,
                                 final String userId,
                                 final String caseRole,
                                 final String organisationID) {
        Set<String> userIds = Set.of(userId);
        try {
            log.info("About to start remove case access {} for users {}", caseRole, userIds);
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                caseRole,
                organisationID,
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

    public void validateBarristerOrgRelationship(CaseData caseData,
                                                 List<String> errorList) {
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
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
                    log.error("Case id {}: Barrister {} is not associated with the organisation {}",
                              caseData.getId(),
                              maskEmail(allocatedBarrister.getBarristerEmail()),
                              allocatedBarrister.getBarristerOrg().getOrganisationID());

                    errorList.add("Barrister doesn't belong to selected organisation");
                });
    }

    public void validateUserRole(CaseData caseData,
                                 String userId,
                                 String userRole,
                                 List<String> errorList) {

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .filter(roleAssignmentResponse ->
                roleAssignmentResponse.getRoleName().equals(userRole)
                    && roleAssignmentResponse.getActorId().equals(userId))
            .findAny()
            .ifPresentOrElse(roleName -> { },
                             () -> {
                                 log.error("Barrister {} is not associated with the case {}",
                                           maskEmail(caseData.getAllocatedBarrister().getBarristerEmail()),
                                           caseData.getId());
                                 errorList.add("Barrister is not associated with the case");
                             });
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
                errorList.add("A barrister is already associated with the case");
            });
    }

    public void validateAddRequest(CaseData caseData, String roleItem, List<String> errorList) {
        validateBarristerOrgRelationship(caseData,errorList);
        validateCaseRoles(caseData,roleItem,errorList);
    }

    public void validateRemoveRequest(CaseData caseData, String userId, String roleItem, List<String> errorList) {
        validateUserRole(caseData, userId, roleItem, errorList);
    }

    public Optional<String> deriveBarristerRole(CaseData caseData) {
        String selectedPartyId = caseData.getAllocatedBarrister().getPartyList().getValueCode();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100BarristerRole(caseData, selectedPartyId);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC401BarristerRole(caseData, selectedPartyId);
        }
        return Optional.empty();
    }

    private Optional<String> getC401BarristerRole(CaseData caseData, String selectedPartyId) {
        String barristerRole = getBarristerCaseRole(
            () -> ElementUtils.wrapElements(caseData.getApplicantsFL401()),
            selectedPartyId,
            DAAPPLICANT,
            partyDetailsElement -> partyDetailsElement.getValue().getPartyId()
        ).orElseGet(() -> getBarristerCaseRole(
            () -> ElementUtils.wrapElements(caseData.getRespondentsFL401()),
            selectedPartyId,
            DARESPONDENT,
            partyDetailsElement -> partyDetailsElement.getValue().getPartyId()
        ).orElse(null));
        return Optional.ofNullable(barristerRole);
    }

    private Optional<String> getC100BarristerRole(CaseData caseData, String selectedPartyId) {
        String barristerRole = getBarristerCaseRole(
            caseData::getApplicants,
            selectedPartyId,
            CAAPPLICANT,
            Element::getId
        ).orElseGet(() -> getBarristerCaseRole(
            caseData::getRespondents,
            selectedPartyId,
            CARESPONDENT,
            Element::getId
            ).orElse(null));

        return Optional.ofNullable(barristerRole);
    }

    private Optional<String> getBarristerCaseRole(Supplier<List<Element<PartyDetails>>> partySupplier,
                                                  String selectedPartyId,
                                                  Representing  representing,
                                                   Function<Element<PartyDetails>, UUID> partyId) {
        List<Element<PartyDetails>> parties = partySupplier.get();

        return IntStream.range(0, parties.size())
            .filter(i -> partyId.apply(parties.get(i)).equals(fromString(selectedPartyId)))
            .findFirst()
            .stream()
            .mapToObj(i -> {
                BarristerRole[] values = Arrays.stream(BarristerRole.values())
                    .filter(barristerRole -> barristerRole.getRepresenting().equals(representing))
                    .toArray(BarristerRole[]::new);
                return Optional.of(values[i].getCaseRoleLabel());
            })
            .findFirst()
            .orElse(Optional.empty());
    }

    public void updatedPartyWithBarristerDetails(CaseData caseData, String barristerRole, String userId) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            updatedPartyWithBarristerDetails(
                caseData,
                barristerRole,
                caseData::getApplicants,
                userId
            ).orElseGet(() -> updatedPartyWithBarristerDetails(
                caseData,
                barristerRole,
                caseData::getRespondents,
                userId
            ).orElse(null));
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            updatedPartyWithBarristerDetails(caseData,
                                             barristerRole,
                                             caseData.getApplicantsFL401(),
                                             userId)
                .orElseGet(() -> updatedPartyWithBarristerDetails(caseData,
                                                                  barristerRole,
                                                                  caseData.getRespondentsFL401(),
                                                                  userId)
                    .orElse(null));
        }
    }

    private Optional<Boolean> updatedPartyWithBarristerDetails(CaseData caseData,
                                                               String barristerRole,
                                                               Supplier<List<Element<PartyDetails>>> parties,
                                                               String userId) {
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
        Optional<Element<PartyDetails>> partyDetailsElement = parties.get().stream()
            .filter(element -> element.getId()
                .equals(fromString(allocatedBarrister.getPartyList().getValueCode())))
            .findFirst();

        if (partyDetailsElement.isPresent()) {
            updateBarrister(barristerRole, partyDetailsElement.get().getValue(), allocatedBarrister, userId);
            return Optional.of(true);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Boolean> updatedPartyWithBarristerDetails(CaseData caseData, String barristerRole, PartyDetails party, String userId) {
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();

        if (party.getPartyId().equals(fromString(allocatedBarrister.getPartyList().getValueCode()))) {
            updateBarrister(barristerRole, party, allocatedBarrister, userId);
            return Optional.of(true);
        } else {
            return Optional.empty();
        }
    }

    private static void updateBarrister(String barristerRole, PartyDetails partyDetails, AllocatedBarrister allocatedBarrister, String userId) {
        partyDetails.setBarrister(
            Barrister.builder()
                .barristerName(allocatedBarrister.getBarristerName())
                .barristerEmail(allocatedBarrister.getBarristerEmail())
                .barristerRole(barristerRole)
                .barristerOrgId(allocatedBarrister.getBarristerOrg().getOrganisationID())
                .barristerId(userId)
                .build()
        );
    }
}
