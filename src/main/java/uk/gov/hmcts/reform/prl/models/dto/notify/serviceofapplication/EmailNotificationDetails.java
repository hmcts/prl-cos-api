package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
public class EmailNotificationDetails {
    @CCD(label = "Documents attached", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("attachedDocs")
    private String attachedDocs;
    @CCD(label = "Recipient email", searchable = false)
    @JsonProperty("emailAddress")
    private String emailAddress;
    @CCD(label = "Sent at", searchable = false)
    @JsonProperty("timeStamp")
    private String timeStamp;
    @CCD(label = "Served party", searchable = false)
    @JsonProperty("servedParty")
    private String servedParty;
    @CCD(label = "Documents", searchable = false)
    @JsonProperty("docs")
    private List<Element<Document>> docs;
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
