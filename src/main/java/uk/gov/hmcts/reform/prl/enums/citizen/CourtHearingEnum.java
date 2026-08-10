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
public enum CourtHearingEnum {

    @CCD(label = "A support worker or carer")
    @JsonProperty("supportworker")
    supportworker("supportworker","A support worker or carer"),
    @CCD(label = "A friend or family member")
    @JsonProperty("familymember")
    familymember("familymember","A friend or family member"),
    @CCD(label = "Assistance / guide dog")
    @JsonProperty("assistance")
    assistance("assistance","Assistance / guide dog"),
    @CCD(label = "Therapy animal")
    @JsonProperty("animal")
    animal("animal","Therapy animal"),
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
    public static CourtHearingEnum getValue(String key) {
        return CourtHearingEnum.valueOf(key);
    }
}
