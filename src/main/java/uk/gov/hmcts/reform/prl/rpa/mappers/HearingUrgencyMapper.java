package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;

@Component
public class HearingUrgencyMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("isCaseUrgent", CommonUtils.getYesOrNoValue(caseData.getIsCaseUrgent()))
            .add("setOutReasonsBelow", caseData.getSetOutReasonsBelow())
            .add("caseUrgencyTimeAndReason", caseData.getCaseUrgencyTimeAndReason())
            .add("effortsMadeWithRespondents", caseData.getEffortsMadeWithRespondents())
            .add(
                "doYouNeedAWithoutNoticeHearing",
                CommonUtils.getYesOrNoValue(caseData.getDoYouNeedAWithoutNoticeHearing())
            )
            .add(
                "areRespondentsAwareOfProceedings",
                CommonUtils.getYesOrNoValue(caseData.getAreRespondentsAwareOfProceedings())
            )
            .add("reasonsForApplicationWithoutNotice", caseData.getReasonsForApplicationWithoutNotice())
            .add(
                "doYouRequireAHearingWithReducedNotice",
                CommonUtils.getYesOrNoValue(caseData.getDoYouRequireAHearingWithReducedNotice())
            )
            .build();
    }
}
