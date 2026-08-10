package uk.gov.hmcts.reform.prl.enums.managedocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentPartyEnum {

    @CCD(label = "Applicant")
    @JsonProperty("applicant")
    APPLICANT("Applicant"),
    @CCD(label = "Respondent")
    @JsonProperty("respondent")
    RESPONDENT("Respondent"),
    @CCD(label = "Cafcass")
    @JsonProperty("cafcass")
    CAFCASS("Cafcass"),
    @CCD(label = "Cafcass Cymru")
    @JsonProperty("cafcassCymru")
    CAFCASS_CYMRU("Cafcass Cymru"),
    @CCD(label = "Local authority")
    @JsonProperty("localAuthority")
    LOCAL_AUTHORITY("Local authority"),
    @CCD(label = "Court")
    @JsonProperty("Court")
    COURT("Court");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocumentPartyEnum getValue(String key) {
        return DocumentPartyEnum.valueOf(key);
    }
}
