package uk.gov.hmcts.reform.prl.models.dto.bulkprint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintDetails {
    private List<GeneratedDocumentInfo> printedDocs;
    private String bulkPrintId;
}
