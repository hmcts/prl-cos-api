package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class SupportingEvidenceBundle {

    @JsonProperty("name")
    private final String name;
    @JsonProperty("notes")
    private final String notes;
    @JsonProperty("document")
    private final Document document;
    @JsonProperty("documentAcknowledge")
    private final List<DocumentAcknowledge> documentAcknowledge;
    @JsonProperty("dateTimeUploaded")
    private LocalDateTime dateTimeUploaded;
    @JsonProperty("uploadedBy")
    private String uploadedBy;
    @JsonProperty("documentRelatedToCase")
    private final YesOrNo documentRelatedToCase;

}
