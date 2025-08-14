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
public enum CourtHearingEnum {

    @JsonProperty("supportworker")
    supportworker("supportworker","A support worker or carer"),
    @JsonProperty("familymember")
    familymember("familymember","A friend or family member"),
    @JsonProperty("assistance")
    assistance("assistance","Assistance / guide dog"),
    @JsonProperty("animal")
    animal("animal","Therapy animal"),
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
    public static CourtHearingEnum getValue(String key) {
        return CourtHearingEnum.valueOf(key);
    }
}
