package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class Order {

    private LocalDate dateIssued;
    private LocalDate endDate;
    private YesOrNo orderCurrent;
    private String courtName;

}


