package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GatekeepingDetailsService {

    public GatekeepingDetails getGatekeepingDetails(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                                    RefDataUserService refDataUserService) {
        return mapGatekeepingDetails(caseDataUpdated, legalAdviserList, refDataUserService);

    }

    private GatekeepingDetails mapGatekeepingDetails(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList,
                                                     RefDataUserService refDataUserService) {
        GatekeepingDetails.GatekeepingDetailsBuilder gatekeepingDetailsBuilder = GatekeepingDetails.builder();
        if (null != caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)) {
            if (SendToGatekeeperTypeEnum.judge.getId().equalsIgnoreCase(String.valueOf(caseDataUpdated.get(IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING)))
                && null != caseDataUpdated.get("judgeName")) {
                String[] judgePersonalCode = getPersonalCode(caseDataUpdated.get(JUDGE_NAME));
                List<JudicialUsersApiResponse> judgeDetails =
                    refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                     .personalCode(getPersonalCode(caseDataUpdated.get(JUDGE_NAME))).build());
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
            personalCodes[0] = new ObjectMapper().readValue(new ObjectMapper()
                                                                .writeValueAsString(judgeDetails), JudicialUser.class).getPersonalCode();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return personalCodes;
    }

}

