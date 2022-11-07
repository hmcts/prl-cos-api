package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Value
@Builder(toBuilder = true)
public class Order {
    @JsonProperty("orderType")
    private final String orderType;
    @JsonProperty("documentLink")
    private final Document orderDocument;
}
