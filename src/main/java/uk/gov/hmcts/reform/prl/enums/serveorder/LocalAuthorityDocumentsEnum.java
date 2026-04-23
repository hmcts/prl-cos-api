package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LocalAuthorityDocumentsEnum {
    @JsonProperty("childImpactReport1La")
    childImpactReport1La("childImpactReport1La", "Child Impact Report 1"),
    @JsonProperty("childImpactReport2La")
    childImpactReport2La("childImpactReport2La", "Child Impact Report 2"),
    @JsonProperty("sec37Report")
    sec37Report("sec37Report", "Section 37 Report"),
    @JsonProperty("section7ReportLa")
    section7ReportLa("section7ReportLa", "Section 7 report"),
    @JsonProperty("section7AddendumReportLa")
    section7AddendumReportLa("section7AddendumReportLa", "Section 7 Addendum"),
    @JsonProperty("localAuthorityInvolvementLa")
    localAuthorityInvolvementLetter("localAuthorityInvolvementLa", "Local Authority Involvement Letter"),
    @JsonProperty("section47La")
    section47La("section47La", "Section 47 Enquiry"),
    @JsonProperty("cirExtensionRequestLa")
    cirExtensionRequestLa("cirExtensionRequestLa", "Child Impact Report extension request"),
    @JsonProperty("cirTransferRequestLa")
    cirTransferRequestLa("cirTransferRequestLa", "Transfer request"),
    @JsonProperty("localAuthorityOtherDoc")
    localAuthorityOtherDoc("localAuthorityOtherDoc", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonCreator
    public static LocalAuthorityDocumentsEnum getValue(String key) {
        return LocalAuthorityDocumentsEnum.valueOf(key);
    }
}

