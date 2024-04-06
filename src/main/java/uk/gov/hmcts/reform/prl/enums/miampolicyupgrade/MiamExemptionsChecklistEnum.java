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
    DOMESTIC_ABUSE("domesticAbuse", "Domestic abuse"),
    @JsonProperty("childProtectionConcern")
    CHILD_PROTECTION_CONCERN("childProtectionConcern", "Child protection concerns"),
    @JsonProperty("urgency")
    URGENCY("urgency","Urgency"),
    @JsonProperty("previousMIAMattendance")
    PREVIOUS_MIAM_ATTENDANCE("previousMIAMattendance", "Previous MIAM attendance or previous MIAM exemption "),
    @JsonProperty("other")
    OTHER("other","Other");

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
