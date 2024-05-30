package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamExemptionsChecklistEnum {

    @JsonProperty("mpuDomesticAbuse")
    mpuDomesticAbuse("mpuDomesticAbuse", "Domestic abuse"),
    @JsonProperty("mpuChildProtectionConcern")
    mpuChildProtectionConcern("mpuChildProtectionConcern", "Child protection concerns"),
    @JsonProperty("mpuUrgency")
    mpuUrgency("mpuUrgency","Urgency"),
    @JsonProperty("mpuPreviousMiamAttendance")
    mpuPreviousMiamAttendance("mpuPreviousMiamAttendance", "Previous attendance of a MIAM or non-court dispute resolution"),
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
