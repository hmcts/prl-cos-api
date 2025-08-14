package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CaseDetails {

    @JsonProperty("id")
    private String caseId;

    private String state;

    @JsonProperty("case_data")
    private CaseData caseData;
}
