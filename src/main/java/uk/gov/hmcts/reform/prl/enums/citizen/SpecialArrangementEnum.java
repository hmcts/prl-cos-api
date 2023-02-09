package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SpecialArrangementEnum {
    separateWaitingRoom("Separate waiting room"),
    separateExitEntrance("Separate exits and entrances"),
    screenWithOtherPeople("Screens so you and the other people in the case cannot see each other"),
    separateToilets("Separate toilets"),
    visitCourtBeforeHearing("Visit to court before the hearing"),
    videoLinks("Video links"),
    specialArrangementsOther("Other"),
    noSafetyRequirements("No, I do not have any safety requirements at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SpecialArrangementEnum getValue(String key) {
        return SpecialArrangementEnum.valueOf(key);
    }
}
