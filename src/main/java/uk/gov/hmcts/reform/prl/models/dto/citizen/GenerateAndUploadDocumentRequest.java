package uk.gov.hmcts.reform.prl.models.dto.citizen;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.Map;

@Value
@Schema(description = "Request body model for Document Generation Request")
@Data
@Builder
public class GenerateAndUploadDocumentRequest {
    @JsonProperty(value = "values", required = true)
    @Schema(description = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}
