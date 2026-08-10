package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoReportsAlsoSentToEnum {

    @CCD(label = "Other party")
    @JsonProperty("partyOrParties")
    partyOrParties("partyOrParties", "Other party"),

    @CCD(label = "Cafcass or Cafcass Cymru")
    @JsonProperty("cafcassCymru")
    cafcassCymru("cafcassCymru", "Cafcass or Cafcass Cymru");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoReportsAlsoSentToEnum getValue(String key) {
        return SdoReportsAlsoSentToEnum.valueOf(key);
    }
}
