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

        return caseRoleClient.findUserCaseRoles(
            authorisation,
            authTokenGenerator.generate(),
            buildFindUserCaseRolesRequest(caseId, userId)
        );
    }

    private FindUserCaseRolesRequest buildFindUserCaseRolesRequest(String caseId, String userId) {
        return FindUserCaseRolesRequest
            .builder()
            .caseIds(List.of(caseId))
            .userIds(List.of(userId))
            .build();
    }
}
