package uk.gov.hmcts.reform.prl.models.dto.bulkprint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintDetails {
    @CCD(
            label = "Printed documents",
            showCondition = "bulkPrintId=\"NEVER_SHOW\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("printedDocs")
    private String printedDocs;
    @CCD(label = "Bulk print id", searchable = false)
    @JsonProperty("bulkPrintId")
    private String bulkPrintId;
    @CCD(label = "Recipient name", searchable = false)
    @JsonProperty("recipientsName")
    private String recipientsName;
    @CCD(label = "Recipients address", searchable = false, typeOverride = FieldType.AddressUK)
    @JsonProperty("postalAddress")
    private Address postalAddress;
    @CCD(label = "Sent at", searchable = false)
    @JsonProperty("timeStamp")
    private String timeStamp;
    @CCD(label = "Served party", searchable = false)
    @JsonProperty("servedParty")
    private String servedParty;
    @CCD(label = "Print documents", searchable = false)
    @JsonProperty("printDocs")
    private List<Element<Document>> printDocs;
    /**
     * This will contain either
     * 1. Served party id(applicant) after SOA or after confidential check.
     * 2. List of comma separated served party ids(respondents) after statement of service.
     */
    @CCD(label = "Party IDs", showCondition = "partyIds=\"NEVER_SHOW\"", searchable = false)
    @JsonProperty("partyIds")
    private String partyIds;

    @JsonIgnore
    public LocalDateTime getServedDateTime() {
        return LocalDateTime.parse(timeStamp,
                                   DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS);
    }
}
