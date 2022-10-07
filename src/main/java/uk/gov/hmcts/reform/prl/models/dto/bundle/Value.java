package uk.gov.hmcts.reform.prl.models.dto.bundle;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Value
@Builder(toBuilder = true)
public class Value {

    @JsonProperty("documentFileName")
    public String documentFileName;
    @JsonProperty("documentLink")
    public DocumentLink documentLink;
    @JsonProperty("typeOfDocumentFurtherEvidence")
    public String typeOfDocumentFurtherEvidence;
    @JsonProperty("restrictCheckboxFurtherEvidence")
    public List<Object> restrictCheckboxFurtherEvidence = null;

}
