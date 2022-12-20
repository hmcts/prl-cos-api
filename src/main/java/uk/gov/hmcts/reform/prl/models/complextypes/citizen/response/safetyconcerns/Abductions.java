package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.PassportPossessionEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Abductions {
    private final String c1A_abductionReasonOutsideUk;
    private final String c1A_childsCurrentLocation;
    private final YesOrNo c1A_childrenMoreThanOnePassports;
    private final List<PassportPossessionEnum> c1A_possessionChildrenPassport;
    private final String c1A_provideOtherDetails;
    private final YesOrNo c1A_passportOffice;
    private final YesOrNo c1A_abductionPassportOfficeNotified;
    private final String c1A_previousAbductionsShortDesc;
    private final YesOrNo c1A_policeOrInvestigatorInvolved;
    private final String c1A_policeOrInvestigatorOtherDetails;
    private final YesOrNo c1A_childAbductedBefore;
}
