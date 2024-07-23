package uk.gov.hmcts.reform.prl.enums.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SosUploadedByEnum {

    @JsonProperty("COURT_STAFF")
    COURT_STAFF("COURT_STAFF", "Court staff"),
    @JsonProperty("APPLICANT_SOLICITOR")
    APPLICANT_SOLICITOR("APP_LEGAL_REP", "Applicant solicitor"),
    @JsonProperty("APPLICANT_LIP")
    APPLICANT_LIP("APPLICANT_LIP", "Applicant LiP");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SosUploadedByEnum getValue(String key) {
        return SosUploadedByEnum.valueOf(key);
    }
}
