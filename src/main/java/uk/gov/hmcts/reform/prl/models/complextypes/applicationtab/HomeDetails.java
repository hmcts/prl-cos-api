package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;

import java.util.List;

@Builder
@Data
public class HomeDetails {
    private final Address address;
    private final String peopleLivingAtThisAddress;
    private final String everLivedAtTheAddress;
    private final String intendToLiveAtTheAddress;
    private final YesOrNo doAnyChildrenLiveAtAddress;
    private final List<Element<ChildrenLiveAtAddress>> children;

    private final YesOrNo isPropertyAdapted;
    private final YesOrNo howIsThePropertyAdapted;
    private final YesOrNo isThereMortgageOnProperty;
    private final String mortgageNamedAfter;
    private final String mortgageNumber;
    private final String mortgageLenderName;
    private final Address mortgageAddress;
    private final YesOrNo isPropertyRented;
    private final String landLordNamedAfter;
    private final String landlordName;
    private final Address landlordAddress;
    private final YesOrNo doesApplicantHaveHomeRights;
    private final String livingSituation;
    private final String familyHome;
    private final String furtherInformation;
}
