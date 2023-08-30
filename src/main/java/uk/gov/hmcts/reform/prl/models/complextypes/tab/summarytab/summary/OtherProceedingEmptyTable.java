package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtherProceedingEmptyTable {
    @JsonProperty("otherProceedingEmptyField")
    private final String otherProceedingEmptyField;
}
