package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AllegationOfHarm {
    private final String typesOfHarm;
}
