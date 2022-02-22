package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonObject;

@Component
public class TypeOfApplicationMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("orderAppliedFor", String.valueOf(caseData.getOrdersApplyingFor()))
            .add("typeOfChildArrangementsOrder", caseData.getTypeOfChildArrangementsOrder() != null ? caseData.getTypeOfChildArrangementsOrder().getDisplayedValue() : null)
            .add("natureOfOrder", caseData.getNatureOfOrder())
            .add("consentOrder", CommonUtils.getYesOrNoValue(caseData.getConsentOrder()))
            .add("applicationPermissionRequired", caseData.getApplicationPermissionRequired() != null ? caseData.getApplicationPermissionRequired().getDisplayedValue() : null)
            .add("applicationPermissionRequiredReason", caseData.getApplicationPermissionRequiredReason())
            .add("applicationDetails", caseData.getApplicationDetails())
            .build();
    }
}
