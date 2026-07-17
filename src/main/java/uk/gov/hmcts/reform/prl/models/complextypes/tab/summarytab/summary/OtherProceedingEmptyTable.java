package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class OtherProceedingEmptyTable {
    @CCD(label = "There are no other proceedings", searchable = false)
    private final String otherProceedingEmptyField;
}
