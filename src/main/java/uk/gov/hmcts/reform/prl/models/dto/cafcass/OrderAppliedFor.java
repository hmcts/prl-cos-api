package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OrderAppliedFor {

    private String ordersApplyingFor;
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
