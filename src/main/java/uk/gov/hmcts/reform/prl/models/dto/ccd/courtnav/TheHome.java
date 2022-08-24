package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class TheHome {

    private final boolean applyingForOccupationOrder;
    private final Address occupationOrderAddress;
    private final List<CurrentResidentAtAddressEnum> currentlyLivesAtAddress;
    private final String currentlyLivesAtAddressOther;
    private final PreviousOrIntendedResidentAtAddressEnum previouslyLivedAtAddress;
    private final PreviousOrIntendedResidentAtAddressEnum intendedToLiveAtAddress;
    private final List<Element<ChildAtAddress>> childrenApplicantResponsibility;
    private final boolean propertySpeciallyAdapted;
    private final String propertySpeciallyAdaptedDetails;
    private final boolean propertyHasMortgage;
    private final List<ContractEnum> namedOnMortgage;
    private final String namedOnMortgageOther;
    private final String mortgageNumber;
    private final String mortgageLenderName;
    private final Address mortgageLenderAddress;
    private final boolean propertyIsRented;
    private final List<ContractEnum> namedOnRentalAgreement;
    private final String namedOnRentalAgreementOther;
    private final String landlordName;
    private final Address landlordAddress;
    private final boolean haveHomeRights;
    private final List<LivingSituationOutcomeEnum> wantToHappenWithLivingSituation;
    private final List<FamilyHomeOutcomeEnum> wantToHappenWithFamilyHome;
    private final String anythingElseForCourtToConsider;
}
