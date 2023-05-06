package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoRemoteHearingEnum {

    @JsonProperty("cvp")
    cvp("cvp", "CVP"),
    @JsonProperty("teams")
    teams("teams", "Teams"),
    @JsonProperty("btMeetMeTelephone")
    btMeetMeTelephone("btMeetMeTelephone", "BT meet me telephone");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoRemoteHearingEnum getValue(String key) {
        return SdoRemoteHearingEnum.valueOf(key);
    }
}
