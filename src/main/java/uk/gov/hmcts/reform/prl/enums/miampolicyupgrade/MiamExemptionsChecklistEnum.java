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

    @JsonProperty("domesticAbuse")
    domesticAbuse("domesticAbuse", "Domestic abuse"),
    @JsonProperty("childProtectionConcern")
    childProtectionConcern("childProtectionConcern", "Child protection concerns"),
    @JsonProperty("urgency")
    urgency("urgency","Urgency"),
    @JsonProperty("previousMiamAttendance")
    previousMiamAttendance("previousMiamAttendance", "Previous attendance of a MIAM or non-court dispute resolution"),
    @JsonProperty("other")
    other("other","Other");

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
