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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.exception.InvalidPartyException;
import uk.gov.hmcts.reform.prl.exception.InvalidSolicitorRoleException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

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
    private final MaskEmail maskEmail;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;
    private final BarristerHelper casehelper;
    private final BarristerRemoveService barristerRemoveService;

    private InvalidPartyException getInvalidPartyException(CaseData caseData,
                                                                  String selectedPartyId) {
        log.error(
            "On case id {} no party found for {}",
            caseData.getId(),
            selectedPartyId
        );
        return new InvalidPartyException("Invalid party selected");
    }

    private void updateBarrister(String barristerRole, PartyDetails partyDetails, AllocatedBarrister allocatedBarrister, String userId) {
        partyDetails.setBarrister(
            Barrister.builder()
                .barristerFirstName(allocatedBarrister.getBarristerFirstName())
                .barristerLastName(allocatedBarrister.getBarristerLastName())
                .barristerEmail(allocatedBarrister.getBarristerEmail())
                .barristerRole(barristerRole)
                .barristerOrg(allocatedBarrister.getBarristerOrg())
                .barristerId(userId)
                .build()
        );
    }

    public void addBarrister(CaseData caseData,
                             String userId,
                             String barristerRole,
                             AllocatedBarrister allocatedBarrister) {
        log.info(
            "On case id {}, about to add {} case access for users {}",
            caseData.getId(),
            barristerRole,
            userId
        );
        grantBarristerCaseAccess(
            caseData,
            userId,
            barristerRole,
            allocatedBarrister
        );
        updatedPartyWithBarristerDetails(
            caseData,
            userId,
            barristerRole,
            allocatedBarrister
        );
    }

    private void grantBarristerCaseAccess(final CaseData caseData,
                                          final String userId,
                                          final String caseRole,
                                          AllocatedBarrister allocatedBarrister) {
        Set<String> userIds = Set.of(userId);
        try {
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

    public void removeBarrister(final CaseData caseData, PartyDetails selectedParty) {
        removeAmBarristerCaseRole(caseData, selectedParty);
        selectedParty.setBarrister(null);
    }

    public void removeAmBarristerCaseRole(final CaseData caseData,
                                Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap) {
        if (featureToggleService.isBarristerFeatureEnabled()) {
            selectedPartyDetailsMap.values().stream()
                .map(Element::getValue)
                .filter(partyDetails -> partyDetails.getBarrister() != null
                    && partyDetails.getBarrister().getBarristerId() != null)
                .forEach(selectedParty -> removeAmBarristerCaseRole(caseData, selectedParty));
        }
    }

    private void removeAmBarristerCaseRole(CaseData caseData, PartyDetails partyDetails) {
        Barrister barrister = partyDetails.getBarrister();
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
                                                 AllocatedBarrister allocatedBarrister,
                                                 List<String> errorList) {
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
                        "Case id {}: Barrister {} is not registered with the selected organisation {}",
                        caseData.getId(),
                        maskEmail.mask(allocatedBarrister.getBarristerEmail()),
                        allocatedBarrister.getBarristerOrg().getOrganisationID()
                    );

                    errorList.add("Barrister is not registered with the selected organisation");
                });
    }

    private void validateUserRole(CaseData caseData,
                                 String selectedPartyId,
                                 List<String> errorList) {
        PartyDetails selectedParty = getSelectedParty(caseData, selectedPartyId);
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
                        maskEmail.mask(selectedParty.getBarrister().getBarristerEmail()),
                        caseData.getId()
                    );
                    errorList.add("Barrister is not associated with the case");
                });
    }

    public PartyDetails getSelectedParty(CaseData caseData, String selectedPartyId) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100Party(caseData, selectedPartyId);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC401Party(caseData, selectedPartyId);
        }
        throw new  IllegalArgumentException(String.join(":",
                                                        "Invalid case type",
                                                        String.valueOf(caseData.getId())));
    }

    private PartyDetails getC401Party(CaseData caseData, String selectedPartyId) {

        return Stream.of(caseData.getApplicantsFL401(), caseData.getRespondentsFL401())
            .filter(Objects::nonNull)
            .filter(partyDetails -> partyDetails.getPartyId()
                .equals(UUID.fromString(selectedPartyId)))
            .findAny()
            .orElseThrow(() -> getInvalidPartyException(caseData, selectedPartyId));
    }

    private PartyDetails getC100Party(CaseData caseData, String selectedPartyId) {

        return Stream.of(caseData.getApplicants(), caseData.getRespondents())
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(partyDetailsElement -> partyDetailsElement.getId()
                .equals(UUID.fromString(selectedPartyId)))
            .findAny()
            .map(Element::getValue)
            .orElseThrow(() -> getInvalidPartyException(caseData, selectedPartyId));
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
                                   AllocatedBarrister allocatedBarrister,
                                   List<String> errorList) {
        userId.ifPresentOrElse(
            id ->
                barristerRole.ifPresentOrElse(
                    role -> {
                        validateBarristerOrgRelationship(caseData, allocatedBarrister, errorList);
                        validateCaseRoles(caseData, role, errorList);
                    },
                    () -> {
                        errorList.add("Could not map to barrister case role");
                        log.error(
                            "Case id {}, could not map to barrister case role for selected party {}",
                            caseData.getId(),
                            allocatedBarrister.getPartyList().getValueCode()
                        );
                    }
                ),
            () -> errorList.add("Could not find a registered barrister with the email address provided")
        );
    }

    public void validateRemoveRequest(CaseData caseData,
                                      String selectedPartyId,
                                      List<String> errorList) {
        validateUserRole(caseData, selectedPartyId, errorList);
    }

    public Optional<String> deriveBarristerRole(Map<String, Object> data, CaseData caseData,
                                                AllocatedBarrister allocatedBarrister) {
        String selectedPartyId = allocatedBarrister.getPartyList().getValueCode();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100BarristerRole(data, caseData, selectedPartyId);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC401BarristerRole(caseData, selectedPartyId);
        }
        throw new  IllegalArgumentException(String.join(":",
                                                   "Invalid case type",
                                                   String.valueOf(caseData.getId())));
    }

    private Optional<String> getC401BarristerRole(CaseData caseData,
                                                  String selectedPartyId) {
        String barristerRole = get401BarristerCaseRole(
            caseData::getApplicantsFL401,
            selectedPartyId,
            DAAPPLICANT
        ).orElseGet(() -> get401BarristerCaseRole(
            caseData::getRespondentsFL401,
            selectedPartyId,
            DARESPONDENT
        ).orElse(null));
        return Optional.ofNullable(barristerRole);
    }

    private Optional<String> get401BarristerCaseRole(Supplier<PartyDetails> partyDetails,
                                                                  String selectedPartyId,
                                                                  Representing representing) {
        if (partyDetails.get().getPartyId().equals(UUID.fromString(selectedPartyId))) {
            return Arrays.stream(BarristerRole.values())
                .filter(barristerRole -> barristerRole.getRepresenting().equals(representing))
                .map(BarristerRole::getCaseRoleLabel)
                .findFirst();
        }
        return Optional.empty();
    }

    private Optional<String> getC100BarristerRole(Map<String, Object> data,
                                                      CaseData caseData,
                                                      String selectedPartyId) {
        PartyDetails c100Party = getC100Party(caseData, selectedPartyId);
        String nameKey = String.join("-", c100Party.getFirstName(), c100Party.getLastName());
        record PartyInfo(String firstName, String lastName) {}

        return Arrays.stream(BarristerRole.RoleMapping.values())
            .filter(roleMapping -> roleMapping.getRepresenting().equals(BarristerRole.Representing.CAAPPLICANT)
                || roleMapping.getRepresenting().equals(BarristerRole.Representing.CARESPONDENT))
            .filter(roleMap -> data.get(roleMap.getParty()) != null)
            .filter(roleMap -> {
                PartyInfo partyInfo = objectMapper.convertValue(data.get(roleMap.getParty()), PartyInfo.class);

                String partyKey = String.join(
                    "-",
                    partyInfo.firstName,
                    partyInfo.lastName
                );

                return partyKey.equals(nameKey);
            })
            .findFirst()
            .flatMap(roleMapping ->
                         Arrays.stream(BarristerRole.values())
                             .filter(barristerRole -> barristerRole.getRoleMapping().equals(roleMapping))
                             .findFirst()
            )
            .map(BarristerRole::getCaseRoleLabel);
    }

    private void updatedPartyWithBarristerDetails(CaseData caseData,
                                                 String userId,
                                                 String barristerRole,
                                                 AllocatedBarrister allocatedBarrister) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            updatedPartyWithBarristerDetails(
                barristerRole,
                userId,
                allocatedBarrister,
                () -> getC100Party(caseData, allocatedBarrister.getPartyList().getValueCode())
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            updatedPartyWithBarristerDetails(
                barristerRole,
                userId,
                allocatedBarrister,
                () -> getC401Party(caseData, allocatedBarrister.getPartyList().getValueCode())
            );
        }
    }

    private void updatedPartyWithBarristerDetails(String barristerRole,
                                                      String userId,
                                                      AllocatedBarrister allocatedBarrister,
                                                      Supplier<PartyDetails> partyDetailsSupplier) {
        PartyDetails c100Party = partyDetailsSupplier.get();
        updateBarrister(barristerRole, c100Party, allocatedBarrister, userId);
    }

    public void removeAmBarristerIfPresent(CaseDetails caseDetails) {
        if (featureToggleService.isBarristerFeatureEnabled()) {
            CaseData caseData = getCaseData(caseDetails, objectMapper);

            removeBarristerIfPresent(caseData,
                                     caseData.getChangeOrganisationRequestField(),
                                     partyDetailsElement ->
                                         removeAmBarristerCaseRole(caseData,
                                                                   partyDetailsElement.getValue()),
                                     partyDetails -> removeAmBarristerCaseRole(caseData,
                                                                               partyDetails)
            );
        } else {
            log.info("Barrister feature is disabled");
        }
    }

    public void removePartyBarristerIfPresent(CaseData caseData,
                                              ChangeOrganisationRequest changeOrganisationRequest) {
        if (featureToggleService.isBarristerFeatureEnabled()) {
            removeBarristerIfPresent(caseData,
                                     changeOrganisationRequest,
                                     caPartyDetailsElement -> {
                                         casehelper.setAllocatedBarrister(
                                             caPartyDetailsElement.getValue(),
                                             caseData,
                                             caPartyDetailsElement.getId());
                                         barristerRemoveService.notifyBarrister(caseData);
                                         caPartyDetailsElement.getValue().setBarrister(null);
                                     },
                                     daPartyDetails -> {
                                         casehelper.setAllocatedBarrister(daPartyDetails,
                                                                          caseData,
                                                                          daPartyDetails.getPartyId());
                                         barristerRemoveService.notifyBarrister(caseData);
                                         daPartyDetails.setBarrister(null);
                                     }

            );
        } else {
            log.info("Barrister feature is disabled");
        }
    }

    private void removeBarristerIfPresent(CaseData caseData,
                                         ChangeOrganisationRequest changeOrganisationRequest,
                                         Consumer<Element<PartyDetails>> caPartyDetailsElement,
                                         Consumer<PartyDetails> daPartyDetails) {
        String solicitorRole = changeOrganisationRequest.getCaseRoleId().getValue().getCode();
        String barristerRole = getMatchingBarristerRole(solicitorRole);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            getC100SelectedParty(caseData, barristerRole)
                .ifPresent(caPartyDetailsElement);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            getFl401SelectedParty(caseData, barristerRole)
                .ifPresent(daPartyDetails);
        }
    }

    private Optional<PartyDetails> getFl401SelectedParty(CaseData caseData, String barristerRole) {
        return Stream.of(caseData.getApplicantsFL401(), caseData.getRespondentsFL401())
            .filter(Objects::nonNull)
            .filter(partyDetails -> Optional.ofNullable(partyDetails.getBarrister())
                .map(Barrister::getBarristerRole)
                .filter(Objects::nonNull)
                .filter(role -> role.equals(barristerRole))
                .isPresent())
            .findAny();
    }

    private Optional<Element<PartyDetails>> getC100SelectedParty(CaseData caseData, String barristerRole) {
        return Stream.of(caseData.getApplicants(), caseData.getRespondents())
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .filter(partyDetailsElement ->
                        Optional.ofNullable(partyDetailsElement.getValue().getBarrister())
                            .map(Barrister::getBarristerRole)
                            .filter(Objects::nonNull)
                            .filter(role -> role.equals(barristerRole))
                            .isPresent()
            )
            .findAny();
    }

    private String getMatchingBarristerRole(String solicitorRole) {
        return Arrays.stream(BarristerRole.values())
            .filter(barristerRole -> barristerRole.getSolicitorCaseRole().equals(solicitorRole))
            .map(BarristerRole::getCaseRoleLabel)
            .findFirst()
            .orElseThrow(() -> new InvalidSolicitorRoleException("No barrister matching role found for the given solicitor "
                                                                + solicitorRole));
    }
}
