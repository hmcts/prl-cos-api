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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private static IllegalArgumentException getIllegalArgumentException(CaseData caseData) {
        log.error(
            "On case id {} no party found for {}",
            caseData.getId(),
            caseData.getAllocatedBarrister().getPartyList().getValueCode()
        );
        return new IllegalArgumentException("Invalid party selected");
    }

    private static void updateBarrister(String barristerRole, PartyDetails partyDetails, AllocatedBarrister allocatedBarrister, String userId) {
        partyDetails.setBarrister(
            Barrister.builder()
                .barristerName(allocatedBarrister.getBarristerName())
                .barristerEmail(allocatedBarrister.getBarristerEmail())
                .barristerRole(barristerRole)
                .barristerOrg(allocatedBarrister.getBarristerOrg())
                .barristerId(userId)
                .build()
        );
    }

    public void addBarrister(CaseData caseData, String userId, String barristerRole) {
        log.info(
            "On case id {}, about to add {} case access for users {}",
            caseData.getId(),
            barristerRole,
            userId
        );
        grantBarristerCaseAccess(
            caseData,
            userId,
            barristerRole
        );
        updatedPartyWithBarristerDetails(
            caseData,
            userId,
            barristerRole
        );
    }

    private void grantBarristerCaseAccess(final CaseData caseData,
                                          final String userId,
                                          final String caseRole) {
        Set<String> userIds = Set.of(userId);
        try {
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

    public void removeBarrister(final CaseData caseData) {
        PartyDetails selectedParty = getSelectedParty(caseData);
        Barrister barrister = selectedParty.getBarrister();
        Set<String> userIds = Set.of(barrister.getBarristerId());
        try {
            log.info(
                "On case id {}, about to start remove case access {} for users {}",
                caseData.getId(),
                barrister.getBarristerRole(),
                userIds
            );
            CaseAssignmentUserRolesRequest removeCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseData.getId(),
                barrister.getBarristerRole(),
                barrister.getBarristerOrg().getOrganisationID(),
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
                barrister.getBarristerRole(),
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
                user -> {
                },
                () -> {
                    log.error(
                        "Case id {}: Barrister {} is not associated with the organisation {}",
                        caseData.getId(),
                        maskEmail(allocatedBarrister.getBarristerEmail()),
                        allocatedBarrister.getBarristerOrg().getOrganisationID()
                    );

                    errorList.add("Barrister doesn't belong to selected organisation");
                });
    }

    public void validateUserRole(CaseData caseData,
                                 List<String> errorList) {
        PartyDetails selectedParty = getSelectedParty(caseData);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentService
            .getRoleAssignmentForCase(String.valueOf(caseData.getId()));

        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream()
            .filter(roleAssignmentResponse ->
                        roleAssignmentResponse.getRoleName().equals(selectedParty.getBarrister().getBarristerRole())
                            && roleAssignmentResponse.getActorId().equals(selectedParty.getBarrister().getBarristerId()))
            .findAny()
            .ifPresentOrElse(
                roleName -> {
                },
                () -> {
                    log.error(
                        "Barrister {} is not associated with the case {}",
                        maskEmail(caseData.getAllocatedBarrister().getBarristerEmail()),
                        caseData.getId()
                    );
                    errorList.add("Barrister is not associated with the case");
                });
    }

    private PartyDetails getSelectedParty(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100Party(caseData);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC401Party(caseData);
        }
        return null;
    }

    private PartyDetails getC401Party(CaseData caseData) {
        String selectedPartyId = caseData.getAllocatedBarrister().getPartyList().getValueCode();

        return Stream.of(caseData.getApplicantsFL401(), caseData.getRespondentsFL401())
            .filter(Objects::nonNull)
            .filter(partyDetails -> partyDetails.getPartyId()
                .equals(UUID.fromString(selectedPartyId)))
            .findAny()
            .orElseThrow(() -> getIllegalArgumentException(caseData));
    }

    private PartyDetails getC100Party(CaseData caseData) {
        String selectedPartyId = caseData.getAllocatedBarrister().getPartyList().getValueCode();

        return Stream.of(caseData.getApplicants(), caseData.getRespondents())
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(partyDetailsElement -> partyDetailsElement.getId()
                .equals(UUID.fromString(selectedPartyId)))
            .findAny()
            .map(Element::getValue)
            .orElseThrow(() -> getIllegalArgumentException(caseData));
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

    public void validateAddRequest(Optional<String> userId,
                                   CaseData caseData,
                                   Optional<String> barristerRole,
                                   List<String> errorList) {
        userId.ifPresentOrElse(
            id ->
                barristerRole.ifPresentOrElse(
                    role -> {
                        validateBarristerOrgRelationship(caseData, errorList);
                        validateCaseRoles(caseData, role, errorList);
                    },
                    () -> {
                        errorList.add("Could not map to barrister case role");
                        log.error(
                            "Case id {}, could not map to barrister case role for selected party {}",
                            caseData.getId(),
                            caseData.getAllocatedBarrister().getPartyList().getValueCode()
                        );
                    }
                ),
            () -> errorList.add("Could not find barrister with provided email")
        );
    }

    public void validateRemoveRequest(CaseData caseData, List<String> errorList) {
        validateUserRole(caseData, errorList);
    }

    public Optional<String> deriveBarristerRole(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100BarristerRole(caseData);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC401BarristerRole(caseData);
        }
        return Optional.empty();
    }

    private Optional<String> getC401BarristerRole(CaseData caseData) {
        String selectedPartyId = caseData.getAllocatedBarrister().getPartyList().getValueCode();
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

    private Optional<String> getC100BarristerRole(CaseData caseData) {
        String selectedPartyId = caseData.getAllocatedBarrister().getPartyList().getValueCode();

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
                                                  Representing representing,
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
            updatedPartyWithBarristerDetails(
                caseData,
                barristerRole,
                caseData.getApplicantsFL401(),
                userId
            )
                .orElseGet(() -> updatedPartyWithBarristerDetails(
                    caseData,
                    barristerRole,
                    caseData.getRespondentsFL401(),
                    userId
                )
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
}
