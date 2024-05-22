package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoHearingUrgentCheckListEnum {

    @JsonProperty("immediateRisk")
    immediateRisk("immediateRisk", "There is evidence of immediate risk of harm to the child(ren)"),
    @JsonProperty("applicantsCare")
    applicantsCare("applicantsCare","There is evidence to suggest that the respondent seeks to remove the child(ren) from the applicant's care"),
    @JsonProperty("seekToFrustrate")
    seekToFrustrate("seekToFrustrate", "There is evidence to suggest that the respondent would seek to frustrate the process if the application "
        + "is not heard urgently"),
    @JsonProperty("leaveTheJurisdiction")
    leaveTheJurisdiction("leaveTheJurisdiction",  "There is evidence to suggest that the respondent may attempt to leave the jurisdiction with "
        + "the child(ren) if the application is not heard urgently"),
    @JsonProperty("other")
    other("other","Another reason that has not been listed");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoHearingUrgentCheckListEnum getValue(String key) {
        return SdoHearingUrgentCheckListEnum.valueOf(key);
    }
}
