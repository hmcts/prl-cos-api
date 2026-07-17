package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class SpecialArrangements {
    @CCD(label = "Are there any special arrangements?", searchable = false)
    private final String areAnySpecialArrangements;
}
