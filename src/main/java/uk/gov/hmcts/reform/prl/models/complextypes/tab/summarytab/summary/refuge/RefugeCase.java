package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class RefugeCase {
    @CCD(label = "Anyone living in a refuge?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isRefugeCase;
}
