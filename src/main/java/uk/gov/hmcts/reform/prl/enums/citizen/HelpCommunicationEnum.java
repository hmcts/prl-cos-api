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
public enum HelpCommunicationEnum {

    @CCD(label = "Hearing loop (hearing enhancement system)")
    @JsonProperty("hearingloop")
    hearingloop("hearingloop","Hearing loop (hearing enhancement system)"),
    @CCD(label = "Infrared receiver (hearing enhancement system)")
    @JsonProperty("infraredreceiver")
    infraredreceiver("infraredreceiver","Infrared receiver (hearing enhancement system)"),
    @CCD(label = "Need to be close to who is speaking")
    @JsonProperty("needspeakinghelp")
    needspeakinghelp("needspeakinghelp","Need to be close to who is speaking"),
    @CCD(label = "Lip speaker")
    @JsonProperty("lipspeaker")
    lipspeaker("lipspeaker","Lip speaker"),
    @CCD(label = "British Sign Language interpreter")
    @JsonProperty("signlanguage")
    signlanguage("signlanguage","British Sign Language interpreter"),
    @CCD(label = "Speech to text reporter (palantypist)")
    @JsonProperty("speechreporter")
    speechreporter("speechreporter","Speech to text reporter (palantypist)"),
    @CCD(label = "Extra time to think and explain myself")
    @JsonProperty("extratime")
    extratime("extratime","Extra time to think and explain myself"),
    @CCD(label = "Visit to court before the court hearing")
    @JsonProperty("courtvisit")
    courtvisit("courtvisit","Visit to court before the court hearing"),
    @CCD(label = "Explanation of the court hearing room layout and who will be in the room")
    @JsonProperty("courthearing")
    courthearing("courthearing","Explanation of the court hearing room layout and who will be in the room"),
    @CCD(label = "Intermediary")
    @JsonProperty("intermediary")
    intermediary("intermediary","Intermediary"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("other","Other"),
    @CCD(label = "No, I do not need any extra support at this time")
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
