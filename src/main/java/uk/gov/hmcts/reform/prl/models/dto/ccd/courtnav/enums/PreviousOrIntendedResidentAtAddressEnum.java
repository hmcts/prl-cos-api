package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PreviousOrIntendedResidentAtAddressEnum {

    @JsonProperty("applicantAndRespondent")
    applicantAndRespondent("applicantAndRespondent", "Yes, both of them"),

    @JsonProperty("applicant")
    applicant("applicant", "Yes, the applicant"),

    @JsonProperty("respondent")
    respondent("respondent","Yes, the respondent"),

    @JsonProperty("neither")
    neither("neither","No");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PreviousOrIntendedResidentAtAddressEnum getValue(String key) {
        return PreviousOrIntendedResidentAtAddressEnum.valueOf(key);
    }
}
