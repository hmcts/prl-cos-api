package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtherProceedings {
    private final String caseNumber;
    private final String typeOfOrder;
    private final String nameOfCourt;
}
