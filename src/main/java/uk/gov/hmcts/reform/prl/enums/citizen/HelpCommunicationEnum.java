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
public enum HelpCommunicationEnum {

    @JsonProperty("hearingloop")
    hearingloop("hearingloop","Hearing loop (hearing enhancement system)"),
    @JsonProperty("infraredreceiver")
    infraredreceiver("infraredreceiver","Infrared receiver (hearing enhancement system)"),
    @JsonProperty("needspeakinghelp")
    needspeakinghelp("needspeakinghelp","Need to be close to who is speaking"),
    @JsonProperty("lipspeaker")
    lipspeaker("lipspeaker","Lip speaker"),
    @JsonProperty("signlanguage")
    signlanguage("signlanguage","British Sign Language interpreter"),
    @JsonProperty("speechreporter")
    speechreporter("speechreporter","Speech to text reporter (palantypist)"),
    @JsonProperty("extratime")
    extratime("extratime","Extra time to think and explain myself"),
    @JsonProperty("courtvisit")
    courtvisit("courtvisit","Visit to court before the court hearing"),
    @JsonProperty("courthearing")
    courthearing("courthearing","Explanation of the court hearing room layout and who will be in the room"),
    @JsonProperty("intermediary")
    intermediary("intermediary","Intermediary"),
    @JsonProperty("other")
    other("other","Other"),
    @JsonProperty("nosupport")
    nosupport("nosupport","No, I do not need any support at this time");

    private final String id;
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
