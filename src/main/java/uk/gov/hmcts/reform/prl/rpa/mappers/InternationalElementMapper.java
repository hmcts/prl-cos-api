package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;

@Component
public class InternationalElementMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add(
                "habitualResidentInOtherState",
                CommonUtils.getYesOrNoValue(caseData.getHabitualResidentInOtherState())
            )
            .add("habitualResidentInOtherStateGiveReason", caseData.getHabitualResidentInOtherStateGiveReason())
            .add("jurisdictionIssueGiveReason", caseData.getJurisdictionIssueGiveReason())
            .add("requestToForeignAuthority",
                 CommonUtils.getYesOrNoValue(caseData.getRequestToForeignAuthority()))
            .add("requestToForeignAuthorityGiveReason", caseData.getHabitualResidentInOtherStateGiveReason())
            .add("jurisdictionIssue", CommonUtils.getYesOrNoValue(caseData.getJurisdictionIssue()))
            .build();
    }
}
