package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LivingSituationEnum {

    @JsonProperty("ableToStayInHome")
    ableToStayInHome("ableToStayInHome", "The applicant wants to be able to stay in their home"),
    @JsonProperty("ableToReturnHome")
    ableToReturnHome("ableToReturnHome", "The applicant wants to be able to return home"),
    @JsonProperty("restrictFromEnterHome")
    restrictFromEnterHome("restrictFromEnterHome", "The applicant doesn’t want the respondent to be able to enter the home"),
    @JsonProperty("awayFromHome")
    awayFromHome("awayFromHome", "The applicant wants to keep the respondent away from the area surrounding their home"),
    @JsonProperty("limitRespondentInHome")
    limitRespondentInHome("limitRespondentInHome", "The applicant wants to limit where in their home the respondent can go");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static LivingSituationEnum getValue(String key) {
        return LivingSituationEnum.valueOf(key);
    }

}
