package uk.gov.hmcts.reform.prl.enums.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SosUploadedByEnum {

    @CCD(label = "Court staff")
    @JsonProperty("COURT_STAFF")
    COURT_STAFF("COURT_STAFF", "Court staff"),
    @CCD(label = "Applicant solicitor")
    @JsonProperty("APPLICANT_SOLICITOR")
    APPLICANT_SOLICITOR("APPLICANT_SOLICITOR", "Applicant solicitor"),
    @CCD(label = "Applicant LiP")
    @JsonProperty("APPLICANT_LIP")
    APPLICANT_LIP("APPLICANT_LIP", "Applicant LiP");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SosUploadedByEnum getValue(String key) {
        return SosUploadedByEnum.valueOf(key);
    }
}
