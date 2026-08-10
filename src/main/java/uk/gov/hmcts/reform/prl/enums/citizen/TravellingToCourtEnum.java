package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TravellingToCourtEnum {

    @CCD(label = "Parking space close to the venue")
    @JsonProperty("parkingspace")
    parkingspace("parkingspace","Parking space close to the venue"),
    @CCD(label = "Step free / wheelchair access")
    @JsonProperty("stepfree")
    stepfree("stepfree","Step free / wheelchair access"),
    @CCD(label = "Use of venue wheelchair")
    @JsonProperty("wheelchair")
    wheelchair("wheelchair","Use of venue wheelchair"),
    @CCD(label = "Accessible toilet")
    @JsonProperty("toilet")
    toilet("toilet","Accessible toilet"),
    @CCD(label = "Help using a lift")
    @JsonProperty("lift")
    lift("lift","Help using a lift"),
    @CCD(label = "A different type of chair")
    @JsonProperty("differentchair")
    differentchair("differentchair","A different type of chair"),
    @CCD(label = "Guiding in the building")
    @JsonProperty("building")
    building("building","Guiding in the building"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("other","Other"),
    @CCD(label = "No, I do not need any extra support at this time")
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
