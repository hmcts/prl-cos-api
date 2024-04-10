package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DomesticAbuseEvidenceDocument {

    @JsonProperty("domesticAbuseEvidenceDocument")
    private final Document domesticAbuseEvidenceDocument;
}
