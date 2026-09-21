package uk.gov.hmcts.reform.prl.services.caseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesRequest;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.caseaccess.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Component
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CaseRoleClient caseRoleClient;

    public void removeCreatorRole(String caseId, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String userId = userDetails.getId();

        log.info("CaseID: {} removing [CREATOR] case roles from user {}", caseId, userId);

        caseRoleClient.removeCaseRoles(
            authorisation,
            authTokenGenerator.generate(),
            buildRemoveUserRolesRequest(caseId, userId)
        );

        log.info("CaseID: {} removed [CREATOR] case roles from user {}", caseId, userId);
    }

    private RemoveUserRolesRequest buildRemoveUserRolesRequest(String caseId, String userId) {
        return RemoveUserRolesRequest
            .builder()
            .caseUsers(getCaseUsers(caseId, userId))
            .build();
    }

    private CaseUser buildCaseUser(String caseId, String caseRole, String userId) {
        return CaseUser.builder()
            .caseId(caseId)
            .userId(userId)
            .caseRole(caseRole)
            .build();
    }

    private List<CaseUser> getCaseUsers(String caseId, String userId) {
        return asList(
            buildCaseUser(caseId, "[CREATOR]", userId)
        );
    }

    public FindUserCaseRolesResponse findUserCaseRoles(String caseId, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String userId = userDetails.getId();
        FindUserCaseRolesRequest request = buildFindUserCaseRolesRequest(caseId, userId);

        // TEMP DEBUG: local-dev diagnostics for role lookup behaviour.
        log.info("TEMP-DEBUG findUserCaseRoles request: caseId={}, userId={}", caseId, userId);

        FindUserCaseRolesResponse response = caseRoleClient.findUserCaseRoles(
            authorisation,
            authTokenGenerator.generate(),
            request
        );

        int roleCount = response != null && response.getCaseUsers() != null ? response.getCaseUsers().size() : 0;
        String returnedRoles = response != null && response.getCaseUsers() != null
            ? response.getCaseUsers().stream().map(CaseUser::getCaseRole).collect(Collectors.joining(", "))
            : "";
        log.info("TEMP-DEBUG findUserCaseRoles response: caseId={}, userId={}, roleCount={}, roles=[{}]",
                 caseId, userId, roleCount, returnedRoles);

        return response;
    }

    private FindUserCaseRolesRequest buildFindUserCaseRolesRequest(String caseId, String userId) {
        return FindUserCaseRolesRequest
            .builder()
            .caseIds(List.of(caseId))
            .userIds(List.of(userId))
            .build();
    }
}
