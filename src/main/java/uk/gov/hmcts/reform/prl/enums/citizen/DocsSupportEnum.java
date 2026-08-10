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
public enum DocsSupportEnum {

    @CCD(label = "I need documents printed in a particular colour or font")
    @JsonProperty("docsprint")
    docsprint("docsprint","Documents in a specified colour"),
    @CCD(label = "Documents in an easy read format")
    @JsonProperty("docsreadformat")
    docsreadformat("docsreadformat","Documents in an easy read format"),
    @CCD(label = "Braille documents")
    @JsonProperty("brailledocs")
    brailledocs("brailledocs","Braille documents"),
    @CCD(label = "Documents in large print")
    @JsonProperty("largeprintdocs")
    largeprintdocs("largeprintdocs","Documents in large print"),
    @CCD(label = "Audio translation of documents")
    @JsonProperty("docsaudio")
    docsaudio("docsaudio","Audio translation of documents"),
    @CCD(label = "Documents read out to me")
    @JsonProperty("readoutdocs")
    docsReadOut("readoutdocs","Documents read out to me"),
    @CCD(label = "Information emailed to me")
    @JsonProperty("emailInfo")
    emailInfo("emailInfo","Information emailed to me"),
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
    public static DocsSupportEnum getValue(String key) {
        return DocsSupportEnum.valueOf(key);
    }
}
