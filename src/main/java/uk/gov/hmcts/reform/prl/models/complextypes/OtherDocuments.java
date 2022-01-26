package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.models.documents.OtherDocument;

import java.util.List;

@Data
@Builder
public class OtherDocuments {
    @JsonProperty("documentName")
    private final String documentName;
    @JsonProperty("notes")
    private final String notes;
    private final OtherDocument documentOther;
    private final DocTypeOtherDocumentsEnum docTypeOtherDocumentsEnum;
    private final List<RestrictToCafcassHmcts> restrictCheckboxOtherDocuments;
}
