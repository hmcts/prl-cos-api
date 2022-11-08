package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class Data {
    @JsonProperty("applicantCaseName")
    private final String applicantCaseName;

    @JsonProperty("caseNumber")
    private final String caseNumber;

    @JsonProperty("furtherEvidences")
    private List<FurtherEvidence> furtherEvidences;

    @JsonProperty("otherDocuments")
    private List<OtherDocument> otherDocuments;

    @JsonProperty("applications")
    private List<Applications> applications;

    @JsonProperty("orders")
    private final List<Element<Order>> orders;
}
