package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.GrantType;
import uk.gov.hmcts.reform.prl.enums.RoleType;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.Attributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleRequest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentServiceForSystemUser {

    private static final String ROLE_ASSIGNMENT_HEARING_MANAGER = "case-allocator";
    private static final String ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS = "private-law-system-users";
    private static final String ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE = "private-law-case-allocator-system-user";
    private static final String ROLE_ASSIGNMENT_CLASSIFICATION = "PUBLIC";
    private static final String ROLE_ASSIGNMENT_ROLE_CATEGORY = "SYSTEM";
    private static final String ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION = "PRIVATELAW";
    private static final String ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE = "PRLAPPS";
    private static final String ROLE_ASSIGNMENT_ACTOR_ID_TYPE = "IDAM";

    private final SystemUserService systemUserService;

    private final RoleAssignmentApi roleAssignmentApi;

    private final AuthTokenGenerator authTokenGenerator;

    public void assignHearingRoleToSysUser() {
        String systemUserToken = systemUserService.getSysUserToken();
        String systemUserIdamID = systemUserService.getUserId(systemUserToken);

        log.info("System user IDAM ID generation successful");

        List<RequestedRoles> roleAssignmentList =
                Arrays.asList(
                        buildRoleAssignment(systemUserIdamID, ROLE_ASSIGNMENT_HEARING_MANAGER));

        RoleRequest roleRequest = RoleRequest.roleRequest()
                .assignerId(systemUserIdamID)
                .process(ROLE_ASSIGNMENT_ROLE_REQUEST_PROCESS)
                .reference(ROLE_ASSIGNMENT_ROLE_REQUEST_REFERENCE)
                .replaceExisting(true)
                .build();

        RoleAssignmentRequest roleAssignmentRequestResource =
                RoleAssignmentRequest.roleAssignmentRequest()
                        .roleRequest(roleRequest)
                        .requestedRoles(roleAssignmentList)
                        .build();
        log.info("Calling role assignment AM API");
        roleAssignmentApi.updateRoleAssignment(
                systemUserService.getSysUserToken(),
                authTokenGenerator.generate(),
                getCorrelationId(),
                roleAssignmentRequestResource
        );
    }

    private RequestedRoles buildRoleAssignment(String id, String roleName) {
        return RequestedRoles.requestedRoles()
                .actorId(id)
                .roleType(RoleType.ORGANISATION.name())
                .classification(ROLE_ASSIGNMENT_CLASSIFICATION)
                .roleName(roleName)
                .roleCategory(ROLE_ASSIGNMENT_ROLE_CATEGORY)
                .grantType(GrantType.STANDARD.name())
                .attributes(
                        Attributes.attributes()
                                .jurisdiction(ROLE_ASSIGNMENT_ATTRIBUTE_JURISDICTION)
                                .caseType(ROLE_ASSIGNMENT_ATTRIBUTE_CASE_TYPE)
                                .build())
                .actorIdType(ROLE_ASSIGNMENT_ACTOR_ID_TYPE)
                .build();
    }

    private String getCorrelationId() {
        try {
            return UUID.randomUUID().toString();
        } catch (IllegalStateException e) {
            return null;
        }
    }

}
