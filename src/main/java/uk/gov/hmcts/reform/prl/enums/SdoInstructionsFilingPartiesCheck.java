package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoInstructionsFilingPartiesCheck {
    @CCD(label = "Other direction for Instructions on filing bundles")
    @JsonProperty("other")
    other("other", "Other direction for Instructions on filing bundles");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoInstructionsFilingPartiesCheck getValue(String key) {
        return SdoInstructionsFilingPartiesCheck.valueOf(key);
    }
}
