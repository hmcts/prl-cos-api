package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum YesNoBothEnum {

    @JsonProperty("yesBothOfThem")
    yesBothOfThem("yesBothOfThem","Yes, both of them"),

    @JsonProperty("yesApplicant")
    yesApplicant("yesApplicant","Yes, the applicant"),

    @JsonProperty("yesRespondent")
    yesRespondent("yesRespondent","Yes, the respondent"),

    @JsonProperty("no")
    no("no","No");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PeopleLivingAtThisAddressEnum getValue(String key) {
        return PeopleLivingAtThisAddressEnum.valueOf(key);
    }

}
