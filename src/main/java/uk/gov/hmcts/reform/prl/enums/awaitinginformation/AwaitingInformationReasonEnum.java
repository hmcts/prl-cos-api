package uk.gov.hmcts.reform.prl.enums.awaitinginformation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AwaitingInformationReasonEnum {

    @JsonProperty("miamFurtherInformation")
    miamFurtherInformation("miamFurtherInformation", "MIAM - further information required"),
    @JsonProperty("dwpHmrcWhereaboutsUnknown")
    dwpHmrcWhereaboutsUnknown("dwpHmrcWhereaboutsUnknown", "DWP/HMRC - whereabouts unknown"),
    @JsonProperty("applicantFurtherInformation")
    applicantFurtherInformation("applicantFurtherInformation", "Applicant - further information required"),
    @JsonProperty("applicantClarifyConfidentialDetails")
    applicantClarifyConfidentialDetails("applicantClarifyConfidentialDetails", "Applicant - clarify confidential details"),
    @JsonProperty("respondentFurtherInformation")
    respondentFurtherInformation("respondentFurtherInformation", "Respondent - further information required"),
    @JsonProperty("helpWithFeesFurtherAction")
    helpWithFeesFurtherAction("helpWithFeesFurtherAction", "Help with Fees - further action required"),
    @JsonProperty("ctscRefundRequired")
    ctscRefundRequired("ctscRefundRequired", "CTSC - Refund required"),
    @JsonProperty("other")
    other("other", "Another reason that has not been listed");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AwaitingInformationReasonEnum getValue(String key) {
        return AwaitingInformationReasonEnum.valueOf(key);
    }

}
