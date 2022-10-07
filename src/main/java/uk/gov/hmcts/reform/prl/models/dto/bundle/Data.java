
package uk.gov.hmcts.reform.prl.models.dto.bundle;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class Data {
    @JsonProperty("furtherEvidences")
    public List<FurtherEvidence> furtherEvidences;
    @JsonProperty("otherDocuments")
    public List<OtherDocument> otherDocuments;
}
