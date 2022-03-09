package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;

@Component
public class LitigationCapacityMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("litigationCapacityFactors", caseData.getLitigationCapacityFactors())
            .add("litigationCapacityReferrals", caseData.getLitigationCapacityReferrals())
            .add(
                "litigationCapacityOtherFactors",
                CommonUtils.getYesOrNoValue(caseData.getLitigationCapacityOtherFactors())
            )
            .add("litigationCapacityOtherFactorsDetails", caseData.getLitigationCapacityOtherFactorsDetails())
            .build();
    }
}
