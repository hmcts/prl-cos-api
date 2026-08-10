package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoApplicantRespondentEnum {

    @CCD(label = "Applicant")
    @JsonProperty("applicant")
    applicant("applicant", "Applicant"),
    @CCD(label = "Respondent")
    @JsonProperty("respondent")
    respondent("respondent", "Respondent");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoApplicantRespondentEnum getValue(String key) {
        return SdoApplicantRespondentEnum.valueOf(key);
    }
}
