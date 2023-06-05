package uk.gov.hmcts.reform.prl.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class FeesData {
    private BigDecimal totalAmount;
    private List<FeeDto> fees;
}
