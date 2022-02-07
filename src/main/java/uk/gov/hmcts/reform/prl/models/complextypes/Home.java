package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

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
    private final List<Element<ChildrenLiveAtAddress>> doAnyChildrenLiveAtAddressYes;
    private final YesOrNo propertyAdaptedYesOrNo;
    private final String propertyAdaptedYesOrNoYes;
    private final YesOrNo mortgageOnPropertyYesOrNo;
    private final List<Element<Mortgage>> mortgageOnPropertyYesOrNoIsYes;
    private final YesOrNo isPropertyRentedYesNo;
    private final List<Element<RentedProperty>> isPropertyRentedYesNoIsYes;
    private final YesOrNo applicantHomeRightYesOrNo;
    private final List<LivingSituationEnum> livingSituation;
    private final List<FamilyHomeEnum> familyHome;
    private final String furtherInformation;
}
