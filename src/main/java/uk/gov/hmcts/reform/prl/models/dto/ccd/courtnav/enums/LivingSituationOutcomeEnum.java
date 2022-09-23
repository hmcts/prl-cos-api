package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LivingSituationOutcomeEnum {

    @JsonProperty("stayInHome")
    stayInHome("stayInHome", "The applicant wants to be able to stay in their home"),

    @JsonProperty("returnToHome")
    returnToHome("returnToHome", "The applicant wants to be able to return home"),

    @JsonProperty("respondentNotEnterHome")
    respondentNotEnterHome("respondentNotEnterHome",
                           "The applicant doesn’t want the respondent to be able to enter the home"),

    @JsonProperty("respondentAwayFromSurroundingArea")
    respondentAwayFromSurroundingArea("respondentAwayFromSurroundingArea",
                                      "The applicant wants to keep the respondent away from the area "
                                          + "surrounding their home"),

    @JsonProperty("respondentLimitInHome")
    respondentLimitInHome("respondentLimitInHome",
                          "The applicant wants to limit where in their home the respondent can go");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static LivingSituationOutcomeEnum getValue(String key) {
        return LivingSituationOutcomeEnum.valueOf(key);
    }
}
