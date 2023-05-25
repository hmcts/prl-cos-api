package uk.gov.hmcts.reform.prl.models.dto.bulkprint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintDetails {
    private String printedDocs;
    private String bulkPrintId;
    private String recipientsName;
    private String timeStamp;
}
