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
public enum ReasonableAdjustmentsEnum {

    @JsonProperty("docsformat")
    docsformat("docsformat","I need documents in an alternative format"),
    @JsonProperty("commhelp")
    commhelp("commhelp","I need help communicating and understanding"),
    @JsonProperty("hearingsupport")
    hearingsupport("hearingsupport","I need to bring support with me to a hearing"),
    @JsonProperty("hearingcomfort")
    hearingcomfort("hearingcomfort","I need something to feel comfortable during a hearing"),
    @JsonProperty("travellinghelp")
    travellinghelp("travellinghelp","I need help travelling to, or moving around court buildings"),
    @JsonProperty("nosupport")
    nosupport("nosupport","No, I do not need any extra support at this time");

    private final String id;
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
