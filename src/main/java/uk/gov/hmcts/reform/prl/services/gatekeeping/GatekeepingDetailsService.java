package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATE_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GatekeepingDetailsService {

    private final RoleAssignmentService roleAssignmentService;

    public GatekeepingDetails getGatekeepingDetails(String authorisation, Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                                    RefDataUserService refDataUserService) {
        return mapGatekeepingDetails(authorisation, caseDataUpdated, legalAdviserList, refDataUserService);

    }

    private GatekeepingDetails mapGatekeepingDetails(String authorisation, Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                                     RefDataUserService refDataUserService) {
        GatekeepingDetails.GatekeepingDetailsBuilder gatekeepingDetailsBuilder = GatekeepingDetails.builder();
        if (null != caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)) {
            if (SendToGatekeeperTypeEnum.judge.getId().equalsIgnoreCase(String.valueOf(caseDataUpdated.get(
                IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)))
                && null != caseDataUpdated.get("judgeName")) {
                String[] judgePersonalCode = getPersonalCode(caseDataUpdated.get(JUDGE_NAME));
                String[] judgeIdamIds = getIdamId(caseDataUpdated.get(JUDGE_NAME));
                roleAssignmentService.createRoleAssignment(authorisation, CaseDetails.builder()
                    .jurisdiction(caseDataUpdated.get(JURISDICTION).toString())
                    .caseTypeId(caseDataUpdated.get(CASE_TYPE).toString())
                    .id(Long.valueOf(caseDataUpdated.get("id").toString()))
                    .build(), false, judgeIdamIds[0], ALLOCATE_JUDGE_ROLE);
                List<JudicialUsersApiResponse> judgeDetails =
                    refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                     .personalCode(getPersonalCode(caseDataUpdated.get(
                                                                         JUDGE_NAME))).build());
                gatekeepingDetailsBuilder.isSpecificGateKeeperNeeded(YesOrNo.Yes);
                gatekeepingDetailsBuilder.isJudgeOrLegalAdviserGatekeeping((SendToGatekeeperTypeEnum.judge));
                if (null != judgeDetails && judgeDetails.size() > 0) {
                    gatekeepingDetailsBuilder.judgeName(JudicialUser.builder()
                                                            .personalCode(getPersonalCode(caseDataUpdated.get(JUDGE_NAME))[0]).build());
                    gatekeepingDetailsBuilder.judgePersonalCode(judgePersonalCode[0]);

                }
            } else if (null != legalAdviserList && null != legalAdviserList.getValue()) {
                gatekeepingDetailsBuilder.isSpecificGateKeeperNeeded(YesOrNo.Yes);
                gatekeepingDetailsBuilder.isJudgeOrLegalAdviserGatekeeping((SendToGatekeeperTypeEnum.legalAdviser));
                gatekeepingDetailsBuilder.legalAdviserList(legalAdviserList);
            }
        }
        return gatekeepingDetailsBuilder.build();
    }

    private String[] getPersonalCode(Object judgeDetails) {
        String[] personalCodes = new String[3];
        try {
            personalCodes[0] = new ObjectMapper().readValue(
                new ObjectMapper()
                    .writeValueAsString(judgeDetails),
                JudicialUser.class
            ).getPersonalCode();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return personalCodes;
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

