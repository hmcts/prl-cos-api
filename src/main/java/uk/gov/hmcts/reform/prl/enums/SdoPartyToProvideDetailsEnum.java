package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoPartyToProvideDetailsEnum {
    @CCD(label = "Other direction for party to provide details of new partner to Cafcass")
    @JsonProperty("other")
    other("other", "Other direction for party to provide details of new partner to Cafcass");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoPartyToProvideDetailsEnum getValue(String key) {
        return SdoPartyToProvideDetailsEnum.valueOf(key);
    }
}
