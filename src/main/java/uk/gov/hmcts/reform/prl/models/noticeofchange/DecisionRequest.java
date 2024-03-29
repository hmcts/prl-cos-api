package uk.gov.hmcts.reform.prl.models.noticeofchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecisionRequest {

    @JsonProperty("case_details")
    private CaseDetails caseDetails;

    public static DecisionRequest decisionRequest(CaseDetails caseDetails) {
        return DecisionRequest.builder().caseDetails(caseDetails).build();
    }
}
