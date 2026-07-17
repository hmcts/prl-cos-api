package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Abductions {
    @CCD(label = " ", searchable = false)
    private final String c1AabductionReasonOutsideUk;
    @CCD(label = " ", searchable = false)
    private final String c1AchildsCurrentLocation;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AchildrenMoreThanOnePassport;
    @CCD(label = " ", searchable = false)
    private final List<PassportPossessionEnum> c1ApossessionChildrenPassport;
    @CCD(label = " ", searchable = false)
    private final String c1AprovideOtherDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1ApassportOffice;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AabductionPassportOfficeNotified;
    @CCD(label = " ", searchable = false)
    private final String c1ApreviousAbductionsShortDesc;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1ApoliceOrInvestigatorInvolved;
    @CCD(label = " ", searchable = false)
    private final String c1ApoliceOrInvestigatorOtherDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo c1AchildAbductedBefore;
}
