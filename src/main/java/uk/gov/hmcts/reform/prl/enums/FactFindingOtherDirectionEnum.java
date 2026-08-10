package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FactFindingOtherDirectionEnum {
    @CCD(label = "Include further 'directions for fact-finding' hearing")
    @JsonProperty("other")
    other("other", "Other direction for the 'directions for fact-finding' hearing");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FactFindingOtherDirectionEnum getValue(String key) {
        return FactFindingOtherDirectionEnum.valueOf(key);
    }
}
