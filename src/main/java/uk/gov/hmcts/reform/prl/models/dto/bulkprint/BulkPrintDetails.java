package uk.gov.hmcts.reform.prl.models.dto.bulkprint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkPrintDetails {
    @JsonProperty("printedDocs")
    private String printedDocs;
    @JsonProperty("bulkPrintId")
    private String bulkPrintId;
    @JsonProperty("recipientsName")
    private String recipientsName;
    @JsonProperty("timeStamp")
    private String timeStamp;
}
