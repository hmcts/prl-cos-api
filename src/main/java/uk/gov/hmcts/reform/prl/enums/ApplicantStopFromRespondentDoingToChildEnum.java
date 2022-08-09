package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantStopFromRespondentDoingToChildEnum {

    @JsonProperty("applicantStopFromRespondentDoingToChildEnum_Value_1")
    applicantStopFromRespondentDoingToChildEnum_Value_1(
        "Being violent or threatening towards their child or children"),
    @JsonProperty("applicantStopFromRespondentDoingToChildEnum_Value_2")
    applicantStopFromRespondentDoingToChildEnum_Value_2(
        "Harassing or intimidating their child or children"),
    @JsonProperty("applicantStopFromRespondentDoingToChildEnum_Value_3")
    applicantStopFromRespondentDoingToChildEnum_Value_3(
        "Posting or publishing anything about their child or children in print or digitally"),
    @JsonProperty("applicantStopFromRespondentDoingToChildEnum_Value_4")
    applicantStopFromRespondentDoingToChildEnum_Value_4(
        "Contacting their child or children directly without the applicant’s consent"),
    @JsonProperty("applicantStopFromRespondentDoingToChildEnum_Value_5")
    applicantStopFromRespondentDoingToChildEnum_Value_5(
        "Going to or near the child or child's school or Nursery");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantStopFromRespondentDoingToChildEnum getValue(String key) {
        return ApplicantStopFromRespondentDoingToChildEnum.valueOf(key);
    }

    public static ApplicantStopFromRespondentDoingToChildEnum getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("beingViolentOrThreatening")){
            return ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
        } else if (enteredValue.equalsIgnoreCase("harrasingOrIntimidating")) {
            return ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
        } else if (enteredValue.equalsIgnoreCase("publishingAboutChildren")) {
            return ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
        } else if (enteredValue.equalsIgnoreCase("contactingDirectly")) {
            return ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
        } else if (enteredValue.equalsIgnoreCase("goingNearSchoolNursery")) {
            return ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
        }
        return null;
    }

}
