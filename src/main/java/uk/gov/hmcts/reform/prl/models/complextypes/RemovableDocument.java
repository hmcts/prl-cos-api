package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemovableDocument {
    @CCD(label = "Document category", searchable = false)
    private final String categoryName;
    @CCD(label = "Submitted by", searchable = false)
    private final String documentParty;
    @CCD(label = "Submitted date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime documentUploadedDate;
    @CCD(label = "Uploaded by", searchable = false)
    private final String uploadedBy;
    @CCD(label = "Document", searchable = false)
    private final Document document;
}
