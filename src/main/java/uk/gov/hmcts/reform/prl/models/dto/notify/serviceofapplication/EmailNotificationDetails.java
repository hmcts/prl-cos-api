package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailNotificationDetails {
    @JsonProperty("attachedDocs")
    private String attachedDocs;
    @JsonProperty("emailAddress")
    private String emailAddress;
    @JsonProperty("timeStamp")
    private String timeStamp;
    @JsonProperty("servedParty")
    private String servedParty;
    @JsonProperty("docs")
    private List<Element<Document>> docs;
    /**
     * This will contain either
     * 1. Served party id(applicant) after SOA or after confidential check.
     * 2. List of comma separated served party ids(respondents) after statement of service.
     */
    @JsonProperty("partyIds")
    private String partyIds;
}
