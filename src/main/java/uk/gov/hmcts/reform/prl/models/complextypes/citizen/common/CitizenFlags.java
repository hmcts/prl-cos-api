package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;



@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenFlags {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isApplicationViewed;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isAllegationOfHarmViewed;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isAllDocumentsViewed;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isResponseInitiated;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isApplicationToBeServed;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isStatementOfServiceProvided;
}
