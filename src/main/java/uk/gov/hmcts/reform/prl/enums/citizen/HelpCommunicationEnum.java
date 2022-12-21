package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HelpCommunicationEnum {

    hearingloop("Hearing loop (hearing enhancement system)"),
    infraredreceiver("Infrared receiver (hearing enhancement system)"),
    needspeakinghelp("Need to be close to who is speaking"),
    lipspeaker("Lip speaker"),
    lipspeakerhint("hearing person who has been trained to be easily lip read"),
    signlanguage("British Sign Language interpreter"),
    speechreporter("Speech to text reporter (palantypist)"),
    extratime("Extra time to think and explain myself"),
    courtvisit("Visit to court before the court hearing"),
    courthearing("Explanation of the court hearing room layout and who will be in the room"),
    intermediary("Intermediary"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HelpCommunicationEnum getValue(String key) {
        return HelpCommunicationEnum.valueOf(key);
    }
}
