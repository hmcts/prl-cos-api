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

    @JsonProperty("dioDisclosureCourtList")
    private final String dioDisclosureCourtList;

    @JsonCreator
    public SdoDisclosureOfPapersCaseNumber(String caseNumber, String dioDisclosureCourtList) {
        this.caseNumber  = caseNumber;
        this.dioDisclosureCourtList = dioDisclosureCourtList;
    }

}
