package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourtHearingEnum {

    supportworker("A support worker or carer"),
    familymember("A friend or family member"),
    assistance("Assistance / guide dog"),
    animal("Therapy animal"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CourtHearingEnum getValue(String key) {
        return CourtHearingEnum.valueOf(key);
    }
}
