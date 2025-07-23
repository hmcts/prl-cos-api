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
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class CcdCaseAssignmentService {
    private final CaseAssignmentApi caseAssignmentApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator tokenGenerator;
    private final OrganisationService organisationService;
    private final ObjectMapper objectMapper;

    public void addBarrister(final CaseData caseData, String userId, List<String> errorList) {
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
        grantCaseAccess(caseData.getId(),
                        Set.of(userId),
                        allocatedBarrister.getRoleItem(),
                        allocatedBarrister.getBarristerOrg().getOrganisationID());
    }

    private void grantCaseAccess(Long caseId, Set<String> userIds, String caseRole, String orgId) {
        try {
            log.info("About to start granting case access for users {}", userIds);
            CaseAssignmentUserRolesRequest addCaseAssignedUserRolesRequest = buildCaseAssignedUserRequest(
                caseId,
                caseRole,
                orgId,
                userIds);

            caseAssignmentApi.addCaseUserRoles(systemUserService.getSysUserToken(),
                                               tokenGenerator.generate(),
                                               addCaseAssignedUserRolesRequest);
        } catch (FeignException ex) {
            log.error("Could not assign the users to the case", ex);
            throw new GrantCaseAccessException(caseId, userIds, caseRole);
        }
    }

    private CaseAssignmentUserRolesRequest  buildCaseAssignedUserRequest(Long caseId,
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
                        .build()));

    }
}
