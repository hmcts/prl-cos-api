package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReasonableAdjustmentsEnum {

    docsformat("I need documents in an alternative format"),
    commhelp("I need help communicating and understanding"),
    hearingsupport("I need to bring support with me to a hearing"),
    hearingcomfort("I need something to feel comfortable during a hearing"),
    travellinghelp("I need help travelling to, or moving around court buildings"),
    unabletotakecourtproceedings("Is there a reason you are unable to take part in the court proceedings?"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ReasonableAdjustmentsEnum getValue(String key) {
        return ReasonableAdjustmentsEnum.valueOf(key);
    }
}
