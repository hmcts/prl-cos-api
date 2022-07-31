package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class ProceedingDetails {
    private final String caseNumber;
    private final LocalDate orderMadeOn;
    private final String howLongWasTheOrder;
    private final YesOrNo isCurrentOrder;
    private final String nameOfCourtIssuedOrder;
}
