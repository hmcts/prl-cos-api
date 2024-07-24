package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.SecureAccommodationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.SupplementType;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class Supplement {
    private final SupplementType name;
    @JsonProperty("secureAccommodationType")
    private final SecureAccommodationType secureAccommodationType;
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
