package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class WithoutNoticeOrder {
    @CCD(
            label = "*Do you want to apply for the order without giving notice to the respondent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo orderWithoutGivingNotice;
    @CCD(ignore = true)
    private final String reasonForOrderWithoutGivingNotice;
    @CCD(ignore = true)
    private final String futherDetails;
    @CCD(ignore = true)
    private final YesNoDontKnow isRespondentAlreadyInBailCondition;
    @CCD(ignore = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate bailConditionEndDate;
    @CCD(ignore = true)
    private final String anyOtherDtailsForWithoutNoticeOrder;
}
