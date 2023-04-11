package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SdoDisclosureOfPapersCaseNumber {
    @JsonProperty("caseNumber")
    private final String caseNumber;

    @JsonCreator
    public SdoDisclosureOfPapersCaseNumber(String caseNumber) {
        this.caseNumber  = caseNumber;
    }

}
