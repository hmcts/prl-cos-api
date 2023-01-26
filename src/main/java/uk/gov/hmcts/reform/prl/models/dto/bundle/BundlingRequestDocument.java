package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingRequestDocument {
    @JsonProperty("documentLink")
    private final Document documentLink;

    @JsonProperty("documentFileName")
    public String documentFileName;

    @JsonProperty("documentGroup")
    public BundlingDocGroupEnum documentGroup;
}