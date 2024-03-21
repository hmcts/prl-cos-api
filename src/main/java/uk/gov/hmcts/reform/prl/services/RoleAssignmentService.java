package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.Attributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;

@Slf4j
@Service
@Component
@Data
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RoleAssignmentService {
    @Value("${prl.environment}")
    private String environment;

    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;

    public void createRoleAssignment(String authorization,
                                     CaseDetails caseDetails,
                                     RoleAssignmentDto roleAssignmentDto,
                                     String eventName,
                                     boolean replaceExisting,
                                     String roleName) {
        if (!environment.equals("preview")) {

            log.info("Role Assignment called from event - {}", eventName);
            String actorId = populateActorIdFromDto(authorization, roleAssignmentDto);
            String roleCategory = RoleCategory.JUDICIAL.name();
            if (null != actorId) {
                if (null != roleAssignmentDto.getLegalAdviserList()) {
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
    }

    private String populateActorIdFromDto(String authorization, RoleAssignmentDto roleAssignmentDto) {
        if (null != roleAssignmentDto.getLegalAdviserList()) {
            return fetchActorIdFromSelectedLegalAdviser(authorization, roleAssignmentDto.getLegalAdviserList());
        } else if (null != roleAssignmentDto.getJudicialUser()) {
            return getIdamId(roleAssignmentDto.getJudicialUser())[0];
        } else if (null != roleAssignmentDto.getJudgeEmail()) {
            return userService.getUserByEmailId(authorization, roleAssignmentDto.getJudgeEmail()).get(0).getId();
        }
        return null;
    }

    private String fetchActorIdFromSelectedLegalAdviser(String authorization, Object legalAdviserList) {
        String laEmailId = StringUtils.substringBetween(objectMapper.convertValue(
            legalAdviserList,
            DynamicList.class
        ).getValue().getCode(), "(", ")");
        return userService.getUserByEmailId(authorization, laEmailId).get(0).getId();
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

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        Map<String, String> finalRoles = new HashMap<>();
        List<UserDetails> userDetails = userService.getUserByEmailId(authorisation, emailId);
        final String[] idamRoles = {null};
        userDetails.stream().forEach(
            userDetail -> {
                idamRoles[0] = "";
                userDetail.getRoles().stream().forEach(
                    e -> idamRoles[0] = idamRoles[0].concat(e).concat(", ")
                );
                finalRoles.put(userDetail.getId(), idamRoles[0].substring(0, idamRoles[0].lastIndexOf(", ")));
            }
        );

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(),
            null,
            userService.getUserByEmailId(authorisation, emailId).get(0).getId()
        );

        final int[] i = {0};
        roleAssignmentServiceResponse.getRoleAssignmentResponse().stream().forEach(
            roleAssignmentResponse ->
                finalRoles.put(String.valueOf(i[0]++), roleAssignmentResponse.toString())
        );

        return finalRoles;
    }
}
