package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;


@ComplexType(name = "MIAMPolicyUpgradeExemptionsChecklistEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamExemptionsChecklistEnum {

    @CCD(label = "Domestic abuse")
    @JsonProperty("mpuDomesticAbuse")
    mpuDomesticAbuse("mpuDomesticAbuse", "Domestic abuse"),
    @CCD(label = "Child protection concerns")
    @JsonProperty("mpuChildProtectionConcern")
    mpuChildProtectionConcern("mpuChildProtectionConcern", "Child protection concerns"),
    @CCD(label = "Urgency")
    @JsonProperty("mpuUrgency")
    mpuUrgency("mpuUrgency","Urgency"),
    @CCD(label = "Previous attendance of a MIAM or non-court dispute resolution")
    @JsonProperty("mpuPreviousMiamAttendance")
    mpuPreviousMiamAttendance("mpuPreviousMiamAttendance", "Previous attendance of a MIAM or non-court dispute resolution"),
    @CCD(label = "Other")
    @JsonProperty("mpuOther")
    mpuOther("mpuOther","Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamExemptionsChecklistEnum getValue(String key) {
        return MiamExemptionsChecklistEnum.valueOf(key);
    }

}
