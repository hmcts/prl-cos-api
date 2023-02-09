package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TravellingToCourtEnum {

    @JsonProperty("parkingspace")
    parkingspace("parkingspace","Parking space close to the venue"),
    @JsonProperty("stepfree")
    stepfree("stepfree","Step free / wheelchair access"),
    @JsonProperty("wheelchair")
    wheelchair("wheelchair","Use of venue wheelchair"),
    @JsonProperty("toilet")
    toilet("toilet","Accessible toilet"),
    @JsonProperty("lift")
    lift("lift","Help using a lift"),
    @JsonProperty("differentchair")
    differentchair("differentchair","A different type of chair"),
    @JsonProperty("building")
    building("building","Guiding in the building"),
    @JsonProperty("other")
    other("other","Other"),
    @JsonProperty("nosupport")
    nosupport("nosupport","No, I do not need any extra support at this time");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TravellingToCourtEnum getValue(String key) {
        return TravellingToCourtEnum.valueOf(key);
    }
}
