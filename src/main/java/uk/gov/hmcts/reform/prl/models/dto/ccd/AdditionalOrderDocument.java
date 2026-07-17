package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
public class AdditionalOrderDocument {

    @CCD(label = "Uploaded date & time", searchable = false)
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", searchable = false)
    private final String uploadedBy;
    @CCD(label = "Orders served", searchable = false)
    private final String servedOrders;
    @CCD(label = "Documents", searchable = false)
    private final List<Element<Document>> additionalDocuments;
}
