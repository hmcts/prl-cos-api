package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FamilyHomeEnum {

    @JsonProperty("payForRepairs")
    payForRepairs("payForRepairs", "The applicant needs the respondent to pay for or contribute to repairs or maintenance to the home"),
    @JsonProperty("payOrContributeRent")
    payOrContributeRent("payOrContributeRent", "The applicant needs the respondent to pay for or contribute to the rent or the mortgage"),
    @JsonProperty("useHouseholdContents")
    useHouseholdContents("useHouseholdContents", "The applicant needs the use of the furniture or other household contents");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FamilyHomeEnum getValue(String key) {
        return FamilyHomeEnum.valueOf(key);
    }

}
