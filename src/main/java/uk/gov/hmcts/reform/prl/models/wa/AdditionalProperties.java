package uk.gov.hmcts.reform.prl.models.wa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class AdditionalProperties {
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("caseNoteId")
    private String caseNoteId;
    @JsonProperty("additionalApplicationId")
    private String additionalApplicationId;
    @JsonProperty("hearingId")
    private String hearingId;
}
