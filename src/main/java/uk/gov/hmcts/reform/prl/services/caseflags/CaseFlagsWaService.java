package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_PERFORMING_USER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class CaseFlagsWaService {
    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;

    public Map<String, Object> setUpWaTaskForCaseFlags(String authorisation, CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        try {
            log.info("findIfCreatedByCtsc ===>" + objectMapper.writeValueAsString(caseDataUpdated));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        UserDetails userDetails = userService.getUserDetails(authorisation);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
            authorisation,
            authTokenGenerator.generate(),
            null,
            userDetails.getId()
        );

        List<String> roles = roleAssignmentServiceResponse
            .getRoleAssignmentResponse()
            .stream()
            .map(RoleAssignmentResponse::getRoleName)
            .toList();
        try {
            log.info("roles retrieved ===>" + objectMapper.writeValueAsString(roles));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.CTSC.getRoles()::contains)) {
            log.info("setting CTSC");
            caseDataUpdated.put(WA_PERFORMING_USER, UserRoles.CTSC.name());
        } else {
            log.info("setting null");
            caseDataUpdated.put(WA_PERFORMING_USER, null);
        }
        return caseDataUpdated;
    }
}
