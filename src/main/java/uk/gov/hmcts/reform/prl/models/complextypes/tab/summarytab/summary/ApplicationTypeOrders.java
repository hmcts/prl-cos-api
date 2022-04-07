package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ApplicationTypeOrders {

    private final List<String> applicationTypeOrders;

}
