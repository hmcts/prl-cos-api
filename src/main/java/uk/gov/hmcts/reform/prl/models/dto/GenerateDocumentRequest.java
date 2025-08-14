package uk.gov.hmcts.reform.prl.models.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.Map;


@Value
@Schema(description = "Request body model for Document Generation Request")
@Data
@Builder
public class GenerateDocumentRequest {
    @Schema(description = "Name of the template", required = true)
    @JsonProperty(value = "template", required = true)
    @NotBlank
    private final String template;
    @JsonProperty(value = "values", required = true)
    @Schema(description = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}
