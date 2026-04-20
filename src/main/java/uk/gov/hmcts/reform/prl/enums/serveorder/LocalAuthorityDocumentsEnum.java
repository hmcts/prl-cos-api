package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LocalAuthorityDocumentsEnum {
    @JsonProperty("childImpactReport1")
    childImpactReport1("childImpactReport1", "Child Impact Report 1"),
    @JsonProperty("childImpactReport2")
    childImpactReport2("childImpactReport2", "Child Impact Report 2"),
    @JsonProperty("section37Report")
    section37Report("section37Report", "Section 37 Report"),
    @JsonProperty("section7Report")
    section7Report("section7Report", "Section 7 report"),
    @JsonProperty("section7Addendum")
    section7Addendum("section7Addendum", "Section 7 Addendum"),
    @JsonProperty("localAuthorityInvolvementLetter")
    localAuthorityInvolvementLetter("localAuthorityInvolvementLetter", "Local Authority Involvement Letter"),
    @JsonProperty("section47Enquiry")
    section47Enquiry("section47Enquiry", "Section 47 Enquiry"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static LocalAuthorityDocumentsEnum getValue(String key) {
        return LocalAuthorityDocumentsEnum.valueOf(key);
    }
}

