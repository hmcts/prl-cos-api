package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.GrantType;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.RoleType;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.Attributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATE_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;

    public void createRoleAssignment(String authorization,
                                     CaseDetails caseDetails,
                                     boolean replaceExisting,
                                     String actorId,
                                     String roleName) {

        UserDetails userDetails = userService.getUserDetails(authorization);
        var roleCategory = CaseUtils.getUserRole(userDetails);
        log.debug("user: {} has roleCategory: {}", userDetails.getFullName(), roleCategory);

        RoleRequest roleRequest = RoleRequest.roleRequest()
            .assignerId(userDetails.getId())
            .process("CCD")
            .reference(createRoleRequestReference(caseDetails, userDetails.getId()))
            .replaceExisting(replaceExisting)
            .build();


        List<RequestedRoles> requestedRoles = List.of(RequestedRoles.requestedRoles()
                                                          .actorIdType("IDAM")
                                                          .actorId(actorId)
                                                          .roleType(RoleType.CASE.name())
                                                          .roleName(roleName)
                                                          .classification(Classification.RESTRICTED.name())
                                                          .grantType(GrantType.SPECIFIC.name())
                                                          .roleCategory(RoleCategory.JUDICIAL.name())
                                                          .readOnly(false)
                                                          .beginTime(Instant.now())
                                                          .attributes(Attributes.attributes()
                                                                          .jurisdiction(caseDetails.getJurisdiction())
                                                                          .caseType(caseDetails.getCaseTypeId())
                                                                          .caseId(caseDetails.getId().toString())
                                                                          .build())

                                                          .build());

        RoleAssignmentRequest assignmentRequest = RoleAssignmentRequest.roleAssignmentRequest()
            .roleRequest(roleRequest)
            .requestedRoles(requestedRoles)
            .build();
        roleAssignmentApi.updateRoleAssignment(
            authorization,
            authTokenGenerator.generate(),
            null,
            assignmentRequest
        );
    }


    private String createRoleRequestReference(final CaseDetails caseDetails, final String userId) {
        return caseDetails.getId() + "-" + userId;
    }


    public boolean validateIfUserHasRightRoles(
        String authorization,
        CallbackRequest callbackRequest
    ) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String[] judgeIdamIds = getIdamId(caseDataUpdated.get(JUDGE_NAME));
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
            authorization,
            authTokenGenerator.generate(),
            null,
            judgeIdamIds[0]
        );
        return roleAssignmentServiceResponse.getRoleAssignmentResponse()
            .stream()
            .anyMatch(
                roleAssignmentResponse -> roleAssignmentResponse.getRoleName().equals(ALLOCATE_JUDGE_ROLE)
            );
    }

    private String[] getIdamId(Object judgeDetails) {
        String[] idamIds = new String[3];
        try {
            idamIds[0] = new ObjectMapper().readValue(
                new ObjectMapper()
                    .writeValueAsString(judgeDetails),
                JudicialUser.class
            ).getIdamId();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return idamIds;
    }
}
