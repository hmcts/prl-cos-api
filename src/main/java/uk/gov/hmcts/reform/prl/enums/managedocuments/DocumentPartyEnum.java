package uk.gov.hmcts.reform.prl.enums.managedocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentPartyEnum {

    @JsonProperty("applicant")
    APPLICANT("Applicant"),
    @JsonProperty("respondent")
    RESPONDENT("Respondent"),
    @JsonProperty("cafcass")
    CAFCASS("Cafcass"),
    @JsonProperty("cafcassCymru")
    CAFCASS_CYMRU("Cafcass Cymru"),
    @JsonProperty("localAuthority")
    LOCAL_AUTHORITY("Local authority"),
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
