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
public enum SdoRemoteHearingEnum {

    @CCD(label = "CVP")
    @JsonProperty("cvp")
    cvp("cvp", "CVP"),
    @CCD(label = "Teams")
    @JsonProperty("teams")
    teams("teams", "Teams"),
    @CCD(label = "BT meet me telephone")
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
