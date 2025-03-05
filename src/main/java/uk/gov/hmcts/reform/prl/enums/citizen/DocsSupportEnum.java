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
public enum DocsSupportEnum {

    @JsonProperty("docsprint")
    docsprint("docsprint","Documents in a specified colour"),
    @JsonProperty("docsreadformat")
    docsreadformat("docsreadformat","Documents in an easy read format"),
    @JsonProperty("brailledocs")
    brailledocs("brailledocs","Braille documents"),
    @JsonProperty("largeprintdocs")
    largeprintdocs("largeprintdocs","Documents in large print"),
    @JsonProperty("docsaudio")
    docsaudio("docsaudio","Audio translation of documents"),
    @JsonProperty("readoutdocs")
    docsReadOut("readoutdocs","Documents read out to me"),
    @JsonProperty("emailInfo")
    emailInfo("emailInfo","Information emailed to me"),
    @JsonProperty("other")
    other("other","Other"),
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
