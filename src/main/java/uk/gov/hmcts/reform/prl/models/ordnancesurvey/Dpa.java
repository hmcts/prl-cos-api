package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Dpa {
    @JsonProperty("LOCAL_CUSTODIAN_CODE")
    private String localCustodianCode;
    @JsonProperty("LOCAL_CUSTODIAN_CODE_DESCRIPTION")
    private String localCustodianCodeDescription;
}
