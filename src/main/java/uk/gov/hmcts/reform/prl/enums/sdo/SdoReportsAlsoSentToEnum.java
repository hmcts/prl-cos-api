package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoReportsAlsoSentToEnum {

    @JsonProperty("partyOrParties")
    partyOrParties("partyOrParties", "Party(or parties)"),

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
