package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DomesticAbuseOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.FcOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrdersOptionEnum;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadOrderOptions {

    private ChildArrangementOrdersEnum childArrangementOrders;
    private DomesticAbuseOrdersEnum domesticAbuseOrders;
    private FcOrdersEnum fcOrders;
    private OtherOrdersOptionEnum otherOrdersOption;
}
