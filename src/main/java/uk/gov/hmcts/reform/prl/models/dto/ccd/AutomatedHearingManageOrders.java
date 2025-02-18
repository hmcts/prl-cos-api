package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(builderMethodName = "automatedHearingManageOrdersWith")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomatedHearingManageOrders {

    @JsonProperty("ordersHearingDetails")
    @JsonUnwrapped
    private List<Element<HearingData>> ordersHearingDetails;
}
