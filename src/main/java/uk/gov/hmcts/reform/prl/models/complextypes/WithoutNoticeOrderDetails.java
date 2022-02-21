package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class WithoutNoticeOrderDetails {
    @JsonProperty("orderWithoutGivingNotice")
    private final YesOrNo orderWithoutGivingNotice;

    @JsonCreator
    public WithoutNoticeOrderDetails(YesOrNo orderWithoutGivingNotice) {
        this.orderWithoutGivingNotice = orderWithoutGivingNotice;
    }
}
