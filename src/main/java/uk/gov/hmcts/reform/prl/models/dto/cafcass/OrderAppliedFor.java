package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderAppliedFor {

    @CCD(label = "Order applied for", searchable = false)
    private String ordersApplyingFor;
    @CCD(label = "Type of child arrangements order", searchable = false)
    private String typeOfChildArrangementsOrder;

    public void setTypeOfChildArrangementsOrder(String typeOfChildArrangementsOrder) {
        if (ChildArrangementOrderTypeEnum.spendTimeWithOrder.getDisplayedValue().equals(typeOfChildArrangementsOrder)) {
            this.typeOfChildArrangementsOrder = ChildArrangementOrderTypeEnum.spendTimeWithOrder.name();
        } else if (ChildArrangementOrderTypeEnum.liveWithOrder.getDisplayedValue().equals(typeOfChildArrangementsOrder)) {
            this.typeOfChildArrangementsOrder = ChildArrangementOrderTypeEnum.liveWithOrder.name();
        } else if (ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder.getDisplayedValue().equals(
            typeOfChildArrangementsOrder)) {
            this.typeOfChildArrangementsOrder = ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder.name();
        }

    }
}
