package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SpecialMeasuresEnum {

    @JsonProperty("separateWaitingRoom")
    separateWaitingRoom("separateWaitingRoom", "A separate waiting room in the court building"),

    @JsonProperty("seperateEntranceExit")
    seperateEntranceExit("seperateEntranceExit", "A separate entrance and exit from the court building"),

    @JsonProperty("shieldedByScreen")
    shieldedByScreen("shieldedByScreen", "To be shielded by a privacy screen in the courtroom"),

    @JsonProperty("joinByvideoLink")
    joinByvideoLink("joinByvideoLink", "To join the hearing by video link rather than in person");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SpecialMeasuresEnum getValue(String key) {
        return SpecialMeasuresEnum.valueOf(key);
    }
}
