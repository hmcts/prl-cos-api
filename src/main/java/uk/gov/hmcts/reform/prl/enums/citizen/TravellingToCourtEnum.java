package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TravellingToCourtEnum {

    parkingspace("Parking space close to the venue"),
    stepfree("Step free / wheelchair access"),
    wheelchair("Use of venue wheelchair"),
    toilet("Accessible toilet"),
    lift("Help using a lift"),
    differentchair("A different type of chair"),
    building("Guiding in the building"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

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
