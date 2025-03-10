package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PartyRelationshipToTheCaseEnum {

    @JsonProperty("PTOP")
    PTOP("PTOP", "Person to be protected"),
    @JsonProperty("AR")
    AR("AR", "Additional respondent");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static PartyRelationshipToTheCaseEnum getValue(String key) {
        return PartyRelationshipToTheCaseEnum.valueOf(key);
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public static String getDisplayedValue(String enumValue) {
        if ("PTOP".equalsIgnoreCase(enumValue)) {
            return PartyRelationshipToTheCaseEnum.PTOP.displayedValue;
        } else if ("AR".equalsIgnoreCase(enumValue)) {
            return PartyRelationshipToTheCaseEnum.AR.displayedValue;
        }
        return null;
    }
}
