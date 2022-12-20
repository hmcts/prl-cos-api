package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OtherOrganisationOptions {

    @JsonProperty("anotherOrganisation")
    ANOTHER_ORGANISATION_OPTION("anotherOrganisation", "Another organisation (optional)");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OtherOrganisationOptions getValue(String key) {
        return OtherOrganisationOptions.valueOf(key);
    }
}
