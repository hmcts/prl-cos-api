package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantStopFromRespondentDoingEnum {

    @JsonProperty("applicantStopFromRespondentEnum_Value_1")
    applicantStopFromRespondentEnum_Value_1(
        "Being violent or threatening towards them"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_2")
    applicantStopFromRespondentEnum_Value_2(
        "Harassing or intimidating them"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_3")
    applicantStopFromRespondentEnum_Value_3(
        "Posting or publishing about them either in print or digitally"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_4")
    applicantStopFromRespondentEnum_Value_4(
        "Contacting them directly"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_5")
    applicantStopFromRespondentEnum_Value_5(
        "Causing damage to their possessions"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_6")
    applicantStopFromRespondentEnum_Value_6(
        "Causing damage to their home"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_7")
    applicantStopFromRespondentEnum_Value_7(
        "Going to their home"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_8")
    applicantStopFromRespondentEnum_Value_8(
        "Going near their home"),
    @JsonProperty("applicantStopFromRespondentEnum_Value_9")
    applicantStopFromRespondentEnum_Value_9(
        "Going near their workplace");


    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantStopFromRespondentDoingEnum getValue(String key) {
        return ApplicantStopFromRespondentDoingEnum.valueOf(key);
    }


    public static ApplicantStopFromRespondentDoingEnum getDisplayedValueFromEnumString(String enteredValue) {
        if (enteredValue.equalsIgnoreCase("beingViolentOrThreatening")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
        } else if (enteredValue.equalsIgnoreCase("harrasingOrIntimidating")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_2;
        } else if (enteredValue.equalsIgnoreCase("publishingAboutApplicant")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_3;
        } else if (enteredValue.equalsIgnoreCase("contactingApplicant")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_4;
        } else if (enteredValue.equalsIgnoreCase("damagingPossessions")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_5;
        } else if (enteredValue.equalsIgnoreCase("damagingHome")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_6;
        } else if (enteredValue.equalsIgnoreCase("enteringHome")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_7;
        } else if (enteredValue.equalsIgnoreCase("comingNearHome")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_8;
        } else if (enteredValue.equalsIgnoreCase("comingNearWork")) {
            return ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_9;
        }
        return null;
    }

}
