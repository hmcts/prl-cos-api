package uk.gov.hmcts.reform.prl.services.gatekeeping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.GatekeepingTaskTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.JudgeOrLegalAdviserGatekeepingEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.WhoToSendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_NAME;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getIdamId;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getPersonalCode;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class GatekeepingDetailsService {

    public static final String WHO_TO_SEND_TO_GATEKEEPER = "whoToSendTheCaseToForGatekeeping";
    private static final String JUDGE_OR_LEGAL_ADVISER_GATEKEEPING = "judgeOrLegalAdviserForGatekeeping";

    public GatekeepingDetails getGatekeepingDetails(Map<String, Object> caseDataUpdated, DynamicList legalAdviserList) {
        GatekeepingDetails.GatekeepingDetailsBuilder gatekeepingDetailsBuilder = GatekeepingDetails.builder();
        if (null != caseDataUpdated.get(WHO_TO_SEND_TO_GATEKEEPER)) {
            if (WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_JUDGE.getId()
                .equalsIgnoreCase(String.valueOf(caseDataUpdated.get(WHO_TO_SEND_TO_GATEKEEPER)))
                && null != caseDataUpdated.get("judgeName")) {
                String[] judgePersonalCode = getPersonalCode(caseDataUpdated.get(JUDGE_NAME));

                gatekeepingDetailsBuilder.isSpecificGateKeeperNeeded(YesOrNo.Yes);
                gatekeepingDetailsBuilder.isJudgeOrLegalAdviserGatekeeping((SendToGatekeeperTypeEnum.judge));

                if (null != getIdamId(caseDataUpdated.get(JUDGE_NAME))[0]
                    && null != getPersonalCode(caseDataUpdated.get(JUDGE_NAME))[0]) {
                    gatekeepingDetailsBuilder
                        .judgeName(JudicialUser.builder()
                                       .idamId(getIdamId(caseDataUpdated.get(JUDGE_NAME))[0])
                                       .personalCode(getPersonalCode(caseDataUpdated.get(JUDGE_NAME))[0]).build());
                }

                gatekeepingDetailsBuilder.judgePersonalCode(judgePersonalCode[0]);
            } else if (null != legalAdviserList && null != legalAdviserList.getValue()) {
                gatekeepingDetailsBuilder.isSpecificGateKeeperNeeded(YesOrNo.Yes);
                gatekeepingDetailsBuilder.isJudgeOrLegalAdviserGatekeeping((SendToGatekeeperTypeEnum.legalAdviser));
                gatekeepingDetailsBuilder.legalAdviserList(legalAdviserList);
            }
        }
        return gatekeepingDetailsBuilder.build();
    }

    /**
     * This method is used to identify the gatekeeping task type based on the case data.
     *
     * @param caseData The updated case data.
     * @return The gatekeeping task type as an Enum value.
     */
    public GatekeepingTaskTypeEnum identifyGatekeepingTaskType(CaseData caseData) {

        if (ObjectUtils.isEmpty(caseData.getJudgeOrLegalAdviserForGatekeeping())
            || (caseData.getJudgeOrLegalAdviserForGatekeeping()
                    .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_JUDGE))
                && caseData.getJudgeOrLegalAdviserForGatekeeping()
                    .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_LEGAL_ADVISER)))) {

            return GatekeepingTaskTypeEnum.JUDGE_OR_LEGAL_ADVISER;

        } else if (caseData.getJudgeOrLegalAdviserForGatekeeping()
            .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_JUDGE))
            && !caseData.getJudgeOrLegalAdviserForGatekeeping()
            .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_LEGAL_ADVISER))) {

            return GatekeepingTaskTypeEnum.JUDGE;

        } else if (!caseData.getJudgeOrLegalAdviserForGatekeeping()
            .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_JUDGE))
            && caseData.getJudgeOrLegalAdviserForGatekeeping()
            .contains((JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_LEGAL_ADVISER))) {

            return GatekeepingTaskTypeEnum.LEGAL_ADVISER;

        } else {

            return GatekeepingTaskTypeEnum.JUDGE_OR_LEGAL_ADVISER;

        }
    }

}

