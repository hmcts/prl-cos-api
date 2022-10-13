package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderRecipientsEnum {
    @JsonProperty("applicantOrApplicantSolicitor")
    applicantOrApplicantSolicitor("applicantOrApplicantSolicitor", "Applicant/Applicant solicitor"),
    @JsonProperty("respondentOrRespondentSolicitor")
    respondentOrRespondentSolicitor("respondentOrRespondentSolicitor", "Respondent/Respondent solicitor");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderRecipientsEnum getValue(String key) {
        return OrderRecipientsEnum.valueOf(key);
    }
}
