package uk.gov.hmcts.reform.prl.models.dto.citizen;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request body model for Document Generation Request")
@Getter
@Setter
@Builder(toBuilder = true)
public class GenerateAndUploadDocumentRequest {
    @JsonProperty(value = "values", required = true)
    @Schema(description = "Placeholder key / value pairs", required = true)
    private final Map<String, Object> values;
}
