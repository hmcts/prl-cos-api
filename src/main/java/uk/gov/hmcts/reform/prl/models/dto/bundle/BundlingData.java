package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingData {

    @JsonProperty("applicantCaseName")
    private final String applicantCaseName;

    @JsonProperty("caseNumber")
    private final String caseNumber;

    @JsonProperty("orders")
    private final List<BundlingRequestDocument> orders;

    @JsonProperty("citizenUploadedDocuments")
    private List<BundlingRequestDocument> citizenUploadedDocuments;

    @JsonProperty("applications")
    private List<BundlingRequestDocument> applications;

    @JsonProperty("otherDocumentsUploadedByCourtAdmin")
    private List<BundlingRequestDocument> otherDocuments;


}
