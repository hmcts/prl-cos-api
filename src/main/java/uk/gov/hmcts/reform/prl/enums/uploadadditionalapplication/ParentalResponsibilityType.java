package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum ParentalResponsibilityType {

    @JsonProperty("PR_BY_APPLICANT")
    PR_BY_APPLICANT("PR_BY_APPLICANT", "Parental responsibility by applicant"),
    @JsonProperty("PR_BY_RESPONDENT")
    PR_BY_RESPONDENT("PR_BY_RESPONDENT", "Parental responsibility by respondent");


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static ParentalResponsibilityType getValue(String key) {
        return ParentalResponsibilityType.valueOf(key);
    }
}
