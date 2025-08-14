package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Abductions {
    private final String c1AabductionReasonOutsideUk;
    private final String c1AchildsCurrentLocation;
    private final YesOrNo c1AchildrenMoreThanOnePassport;
    private final List<PassportPossessionEnum> c1ApossessionChildrenPassport;
    private final String c1AprovideOtherDetails;
    private final YesOrNo c1ApassportOffice;
    private final YesOrNo c1AabductionPassportOfficeNotified;
    private final String c1ApreviousAbductionsShortDesc;
    private final YesOrNo c1ApoliceOrInvestigatorInvolved;
    private final String c1ApoliceOrInvestigatorOtherDetails;
    private final YesOrNo c1AchildAbductedBefore;
}
