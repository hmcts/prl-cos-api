package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CafcassServiceApplicationEnum {
    @CCD(label = "Cafcass and Cafcass Cymru (optional)")
    @JsonProperty("cafcass")
    cafcass("cafcass", "Cafcass and Cafcass Cymru (optional)");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CafcassServiceApplicationEnum getValue(String key) {
        return CafcassServiceApplicationEnum.valueOf(key);
    }
}
