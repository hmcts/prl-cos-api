package uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_UUUU;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDetails {

    public String createdBy;
    public String orderRecipients;

    public String orderCreatedDate;

    public void setOrderCreatedDate(String orderCreatedDate) {
        this.orderCreatedDate = CommonUtils.getFormattedStringDate(orderCreatedDate, D_MMMM_UUUU);
    }

    public String orderMadeDate;

    public void setOrderMadeDate(String orderMadeDate) {
        this.orderMadeDate =  CommonUtils.getFormattedStringDate(orderMadeDate, D_MMMM_UUUU);;
    }
}
