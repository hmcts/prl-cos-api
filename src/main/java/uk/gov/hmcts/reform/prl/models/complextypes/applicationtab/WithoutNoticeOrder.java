package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Builder
@Data
public class WithoutNoticeOrder {
    private final YesOrNo orderWithoutGivingNotice;
    private final String reasonForOrderWithoutGivingNotice;
    private final String futherDetails;
    private final YesNoDontKnow isRespondentAlreadyInBailCondition;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate bailConditionEndDate;
    private final String anyOtherDtailsForWithoutNoticeOrder;
}
