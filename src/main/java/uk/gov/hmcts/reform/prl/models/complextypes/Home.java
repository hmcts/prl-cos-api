package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Address;

import java.util.List;

@Data
@Builder
public class Home {
    private final Address address;
    private final List<PeopleLivingAtThisAddressEnum> peopleLivingAtThisAddress;
    private final String textAreaSomethingElse;
    private final YesNoBothEnum everLivedAtTheAddress;
    private final YesNoBothEnum everLivedAtTheAddressNo;
    private final YesOrNo doAnyChildrenLiveAtAddress;
    private final ChildrenLiveAtAddress doAnyChildrenLiveAtAddressYes;
    private final YesOrNo propertyAdaptedYesOrNo;
    private final String propertyAdaptedYesOrNoYes;
    private final YesOrNo mortgageOnPropertyYesOrNo;
    private final Mortgage mortgageOnPropertyYesOrNoIsYes;
    private final YesOrNo isPropertyRentedYesNo;
    private final RentedProperty isPropertyRentedYesNoIsYes;
    private final YesOrNo applicantHomeRightYesOrNo;
    private final List<LivingSituationEnum> livingSituation;
    private final List<FamilyHomeEnum> familyHome;
    private final String furtherInformation;
}
