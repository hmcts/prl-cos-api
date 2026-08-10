package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoNewPartnersToCafcassEnum {
    @CCD(label = "Other direction for Party to provide details of new partner to Cafcass Cymru")
    @JsonProperty("other")
    other("other", "Other direction for Party to provide details of new partner to Cafcass Cymru");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoNewPartnersToCafcassEnum getValue(String key) {
        return SdoNewPartnersToCafcassEnum.valueOf(key);
    }
}
