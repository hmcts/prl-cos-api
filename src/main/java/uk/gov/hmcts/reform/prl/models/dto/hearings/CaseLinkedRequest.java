package uk.gov.hmcts.reform.prl.models.dto.hearings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(builderMethodName = "caseLinkedRequestWith")
public class CaseLinkedRequest {
    @JsonProperty("caseReference")
    private String caseReference;
    @JsonProperty("hearingId")
    private String hearingId;
}
