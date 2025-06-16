package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreviousOrIntendedResidentAtAddressEnum;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavHome {

    private boolean applyingForOccupationOrder;
    private CourtNavAddress occupationOrderAddress;
    private List<CurrentResidentAtAddressEnum> currentlyLivesAtAddress;
    private String currentlyLivesAtAddressOther;
    private PreviousOrIntendedResidentAtAddressEnum previouslyLivedAtAddress;
    private PreviousOrIntendedResidentAtAddressEnum intendedToLiveAtAddress;
    private List<ChildAtAddress> childrenApplicantResponsibility;
    private List<ChildAtAddress> childrenSharedResponsibility;
    private boolean propertySpeciallyAdapted;
    private String propertySpeciallyAdaptedDetails;
    private boolean propertyHasMortgage;
    private List<ContractEnum> namedOnMortgage;
    private String namedOnMortgageOther;
    private String mortgageNumber;
    private String mortgageLenderName;
    private CourtNavAddress mortgageLenderAddress;
    private boolean propertyIsRented;
    private List<ContractEnum> namedOnRentalAgreement;
    private String namedOnRentalAgreementOther;
    private String landlordName;
    private CourtNavAddress landlordAddress;
    private boolean haveHomeRights;
    private List<LivingSituationOutcomeEnum> wantToHappenWithLivingSituation;
    private List<FamilyHomeOutcomeEnum> wantToHappenWithFamilyHome;
    private String anythingElseForCourtToConsider;
}
