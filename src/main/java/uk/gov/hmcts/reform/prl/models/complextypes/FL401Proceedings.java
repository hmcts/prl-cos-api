package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.ProceedingOrderDocument;

@Data
@Builder(toBuilder = true)
public class FL401Proceedings {
    private final String nameOfCourt;
    private final String caseNumber;
    private final String typeOfCase;
    private final String anyOtherDetails;
    private final ProceedingOrderDocument uploadRelevantOrder;
}
