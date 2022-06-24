package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ReasonForWithoutNoticeOrder {
    @JsonProperty("reasonForOrderWithoutGivingNotice")
    private final List<ReasonForOrderWithoutGivingNoticeEnum> reasonForOrderWithoutGivingNotice;
    @JsonProperty("futherDetails")
    private final String futherDetails;
}
