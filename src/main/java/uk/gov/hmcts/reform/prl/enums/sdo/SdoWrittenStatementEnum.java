package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoWrittenStatementEnum {

    @JsonProperty("cafcass")
    cafcass("cafcass", "Cafcass"),
    @JsonProperty("cafcassCymru")
    cafcassCymru("cafcassCymru", "Cafcass Cymru"),
    @JsonProperty("localAuthority")
    localAuthority("localAuthority", "Local authority");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoWrittenStatementEnum getValue(String key) {
        return SdoWrittenStatementEnum.valueOf(key);
    }
}
