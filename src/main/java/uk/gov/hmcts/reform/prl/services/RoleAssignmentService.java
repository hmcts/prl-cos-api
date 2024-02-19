package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.GrantType;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.RoleType;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.Attributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignment;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIARY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {

    public static final String NAME_OF_JUDGE_TO_REVIEW_ORDER = "nameOfJudgeToReviewOrder";
    public static final String NAME_OF_LA_TO_REVIEW_ORDER = "nameOfLaToReviewOrder";
    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;

    public void createRoleAssignment(String authorization,
                                     CaseDetails caseDetails,
                                     boolean replaceExisting,
                                     String roleName) {
        String actorId = populateActorId(authorization, (HashMap<String, Object>) caseDetails.getData());
        String roleCategory = RoleCategory.JUDICIAL.name();
        if (null != actorId) {
            if (actorId.split(UNDERSCORE)[1].equals(LEGAL_ADVISER)) {
                roleName = "allocated-legal-adviser";
                roleCategory = RoleCategory.LEGAL_OPERATIONS.name();
            }

            String systemUserToken = systemUserService.getSysUserToken();
            String systemUserId = systemUserService.getUserId(systemUserToken);

            RoleRequest roleRequest = RoleRequest.roleRequest()
                .assignerId(systemUserId)
                .process("CCD")
                .reference(createRoleRequestReference(caseDetails, systemUserId))
                .replaceExisting(replaceExisting)
                .build();
            String actorIdForService = actorId.split(UNDERSCORE)[0];
            List<RequestedRoles> requestedRoles = List.of(RequestedRoles.requestedRoles()
                .actorIdType("IDAM")
                .actorId(actorIdForService)
                .roleType(RoleType.CASE.name())
                .roleName(roleName)
                .classification(Classification.RESTRICTED.name())
                .grantType(GrantType.SPECIFIC.name())
                .roleCategory(roleCategory)
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
                systemUserToken,
                authTokenGenerator.generate(),
                null,
                assignmentRequest
            );
        }
    }


    public void removeRoleAssignments(CaseDetails caseDetails) {

        List<String> roles = List.of("allocated-legal-adviser");
        QueryResponse resp = roleAssignmentApi.queryRoleAssignments(systemUserService.getSysUserToken(), authTokenGenerator.generate(),
                QueryRequest.builder()
                        .attributes(Map.of("caseId", List.of(caseDetails.getId().toString())))
                        .roleName(roles)
                        .validAt(ZonedDateTime.now())
                        .build()
        );
        List<RoleAssignment> currentAllocatedJudges = resp.getRoleAssignmentResponse();
        currentAllocatedJudges
                .stream()
                .filter(role -> roles.contains(role.getRoleName()))
                .forEach(this::deleteRoleAssignment);

    }

    private void deleteRoleAssignment(RoleAssignment roleAssignment) {
        roleAssignmentApi.deleteRoleAssignment(
                systemUserService.getSysUserToken(),
                authTokenGenerator.generate(),
                null,
                roleAssignment.getId()
        );
    }

    private String populateActorId(String authorization, HashMap<String, Object> caseDataUpdated) {

        if (null != caseDataUpdated.get(
            IS_JUDGE_OR_LEGAL_ADVISOR)) {
            return fetchActorIdIfJudge(authorization, caseDataUpdated);
        } else if (null != caseDataUpdated.get(
            IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)) {
            return fetchActorIdIfJudgeIsGatekeeping(authorization, caseDataUpdated);
        } else {
            if (null != caseDataUpdated.get(NAME_OF_JUDGE_TO_REVIEW_ORDER)
                && caseDataUpdated.get(NAME_OF_JUDGE_TO_REVIEW_ORDER).toString().length() > 3) {
                return (getIdamId(caseDataUpdated.get(NAME_OF_JUDGE_TO_REVIEW_ORDER))[0]) + UNDERSCORE + JUDICIARY;
            } else if (null != caseDataUpdated.get(NAME_OF_LA_TO_REVIEW_ORDER)
                && caseDataUpdated.get(NAME_OF_LA_TO_REVIEW_ORDER).toString().length() > 3) {
                return fetchActorIdFromSelectedLegalAdviser(
                    authorization,
                    caseDataUpdated.get(NAME_OF_LA_TO_REVIEW_ORDER)
                );
            }
        }
        return null;
    }

    private String fetchActorIdIfJudge(String authorization, HashMap<String, Object> caseDataUpdated) {
        if (AllocatedJudgeTypeEnum.judge.getId().equalsIgnoreCase(String.valueOf(caseDataUpdated.get(
            IS_JUDGE_OR_LEGAL_ADVISOR)))) {
            return ((null != caseDataUpdated.get(JUDGE_NAME)
                && caseDataUpdated.get(JUDGE_NAME).toString().length() > 3)
                ? getIdamId(caseDataUpdated.get(JUDGE_NAME))[0]
                : getIdamId(caseDataUpdated.get(JUDGE_NAME_EMAIL))[0]) + UNDERSCORE + JUDICIARY;
        } else {
            return fetchActorIdFromSelectedLegalAdviser(authorization, caseDataUpdated.get("legalAdviserList"));
        }
    }

    private String fetchActorIdIfJudgeIsGatekeeping(String authorization, HashMap<String, Object> caseDataUpdated) {
        if (SendToGatekeeperTypeEnum.judge.getId().equalsIgnoreCase(String.valueOf(caseDataUpdated.get(
            IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)))) {
            return (caseDataUpdated.get(JUDGE_NAME) != null && caseDataUpdated.get(JUDGE_NAME).toString().length() > 3
                ? getIdamId(caseDataUpdated.get(JUDGE_NAME))[0]
                : getIdamId(caseDataUpdated.get(JUDGE_NAME_EMAIL))[0]) + UNDERSCORE + JUDICIARY;
        } else {
            return fetchActorIdFromSelectedLegalAdviser(authorization, caseDataUpdated.get("legalAdviserList"));
        }
    }

    private String fetchActorIdFromSelectedLegalAdviser(String authorization, Object legalAdviserList) {
        String laEmailId = StringUtils.substringBetween(objectMapper.convertValue(
            legalAdviserList,
            DynamicList.class
        ).getValue().getCode(), "(", ")");
        return userService.getUserByEmailId(authorization, laEmailId).get(0).getId() + UNDERSCORE + LEGAL_ADVISER;
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
                roleAssignmentResponse -> roleAssignmentResponse.getRoleName().equals(HEARING_JUDGE_ROLE)
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
