package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class Data {
    @JsonProperty("furtherEvidences")
    public List<FurtherEvidence> furtherEvidences;
    @JsonProperty("otherDocuments")
    public List<OtherDocument> otherDocuments;
    @JsonProperty("finalDocument")
    private final Document finalDocument;
    @JsonProperty("finalWelshDocument")
    private final Document finalWelshDocument;
    @JsonProperty("orders")
    private final List<Element<Order>> orders;
}
