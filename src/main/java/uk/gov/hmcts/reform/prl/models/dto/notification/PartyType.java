package uk.gov.hmcts.reform.prl.models.dto.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PartyType {
    @CCD(label = "Applicant")
    @JsonProperty("APPLICANT")
    APPLICANT("APPLICANT", "Applicant"),
    @CCD(label = "Applicant solicitor")
    @JsonProperty("APPLICANT_SOLICITOR")
    APPLICANT_SOLICITOR("APPLICANT_SOLICITOR", "Applicant solicitor"),
    @CCD(label = "Respondent")
    @JsonProperty("RESPONDENT")
    RESPONDENT("RESPONDENT", "Respondent"),
    @CCD(label = "Respondent solicitor")
    @JsonProperty("RESPONDENT_SOLICITOR")
    RESPONDENT_SOLICITOR("RESPONDENT_SOLICITOR", "Respondent solicitor");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PartyType getValue(String key) {
        return PartyType.valueOf(key);
    }
}
