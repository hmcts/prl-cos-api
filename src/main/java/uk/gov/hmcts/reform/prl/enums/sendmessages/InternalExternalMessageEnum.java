package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum InternalExternalMessageEnum {
    @CCD(label = "Internal message")
    @JsonProperty("INTERNAL")
    INTERNAL("INTERNAL", "Internal message"),
    @CCD(label = "External message")
    @JsonProperty("EXTERNAL")
    EXTERNAL("EXTERNAL", "External message");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static InternalExternalMessageEnum getValue(String key) {
        return InternalExternalMessageEnum.valueOf(key);
    }
}
