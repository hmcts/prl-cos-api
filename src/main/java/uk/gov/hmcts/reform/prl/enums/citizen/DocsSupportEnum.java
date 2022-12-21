package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocsSupportEnum {

    docsprint("I need documents printed in a particular colour or font"),
    docsreadformat("Documents in an easy read format"),
    brailledocs("Braille documents"),
    largeprintdocs("Documents in large print"),
    docsaudio("Audio translation of documents"),
    readoutdocs("Documents read out to me"),
    emailInfo("Information emailed to me"),
    other("Other"),
    nosupport("No, I do not need any extra support at this time");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocsSupportEnum getValue(String key) {
        return DocsSupportEnum.valueOf(key);
    }
}
