package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class Fl401TypeOfApplication {
    @CCD(label = "Order applied for:", searchable = false)
    private final String ordersApplyingFor;
    @CCD(
            label = "Is this linked to a Child Arrangements application?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isLinkedToChildArrangementApplication;
    @CCD(label = "Child Arrangements case number:", searchable = false)
    private final String caCaseNumber;

}
