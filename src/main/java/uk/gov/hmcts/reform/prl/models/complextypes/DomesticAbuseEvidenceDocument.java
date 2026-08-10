package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class DomesticAbuseEvidenceDocument {

    @CCD(label = "Upload evidence", categoryID = "MIAMCertificate", searchable = false)
    @JsonProperty("domesticAbuseDocument")
    private final Document domesticAbuseDocument;

    @JsonCreator
    public DomesticAbuseEvidenceDocument(Document domesticAbuseDocument) {
        this.domesticAbuseDocument = domesticAbuseDocument;
    }
}
