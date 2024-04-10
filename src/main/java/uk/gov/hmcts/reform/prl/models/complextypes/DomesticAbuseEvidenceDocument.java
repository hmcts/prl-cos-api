package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
public class DomesticAbuseEvidenceDocument {

    @JsonProperty("domesticAbuseEvidenceDocument")
    private final Document domesticAbuseEvidenceDocument;

    @JsonCreator
    public DomesticAbuseEvidenceDocument(Document domesticAbuseEvidenceDocument) {
        this.domesticAbuseEvidenceDocument = domesticAbuseEvidenceDocument;
    }
}
