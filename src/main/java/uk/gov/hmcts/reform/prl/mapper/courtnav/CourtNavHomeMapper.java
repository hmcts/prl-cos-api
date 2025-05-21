package uk.gov.hmcts.reform.prl.mapper.courtnav;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavHome;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Component
@AllArgsConstructor
public class CourtNavHomeMapper {
    private final CourtNavAddressMapper addressMapper;

    public Home mapHome(CourtNavHome courtNavHome) {
        return Home.builder()
            .address(addressMapper.map(courtNavHome.getOccupationOrderAddress()))
            .peopleLivingAtThisAddress(getPeopleLivingAtThisAddress(courtNavHome))
            .textAreaSomethingElse(courtNavHome.getCurrentlyLivesAtAddressOther())
            .everLivedAtTheAddress(
                courtNavHome.getPreviouslyLivedAtAddress() != null
                    ? YesNoBothEnum.getDisplayedValueFromEnumString(courtNavHome.getPreviouslyLivedAtAddress().getId())
                    : null)
            .intendToLiveAtTheAddress(
                courtNavHome.getIntendedToLiveAtAddress() != null
                    ? YesNoBothEnum.getDisplayedValueFromEnumString(courtNavHome.getIntendedToLiveAtAddress().getId())
                    : null)
            .doAnyChildrenLiveAtAddress(getAnyChildrenLivedAtAddress(courtNavHome))
            .children(getChildrenDetails(courtNavHome)
                          ? mapHomeChildren(courtNavHome)
                          : null)
            .isPropertyAdapted(courtNavHome.isPropertySpeciallyAdapted() ? Yes : No)
            .howIsThePropertyAdapted(courtNavHome.getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavHome.isPropertyHasMortgage() ? Yes : No)
            .mortgages(getMortgageMappingDetails(courtNavHome))
            .isPropertyRented(courtNavHome.isPropertyIsRented() ? Yes : No)
            .landlords(getLandlordMappingDetails(courtNavHome))
            .doesApplicantHaveHomeRights(courtNavHome.isHaveHomeRights() ? Yes : No)
            .livingSituation(
                courtNavHome.getWantToHappenWithLivingSituation() != null
                    ? getLivingSituationDetails(courtNavHome)
                    : null)
            .familyHome(
                courtNavHome.getWantToHappenWithFamilyHome() != null
                    ? getFamilyHomeDetails(courtNavHome)
                    : null)
            .furtherInformation(courtNavHome.getAnythingElseForCourtToConsider())
            .build();
    }


    private Landlord getLandlordMappingDetails(CourtNavHome home) {
        Landlord landlord = null;
        if (home.isPropertyIsRented()) {
            landlord = Landlord.builder()
                .mortgageNamedAfterList((null != home.getNamedOnRentalAgreement())
                                            ? getLandlordDetails(home) : null)
                .textAreaSomethingElse(home.getNamedOnRentalAgreementOther())
                .landlordName(home.getLandlordName())
                .address(addressMapper.map(home.getLandlordAddress()))
                .build();
        }
        return landlord;
    }

    private Mortgage getMortgageMappingDetails(CourtNavHome home) {
        Mortgage mortgage = null;

        if (home.isPropertyHasMortgage()) {
            mortgage = Mortgage.builder()
                .mortgageNamedAfter((null != home.getNamedOnMortgage())
                                        ? getMortgageDetails(home) : null)
                .textAreaSomethingElse(home.getNamedOnMortgageOther())
                .mortgageLenderName(home.getMortgageLenderName())
                .mortgageNumber(home.getMortgageNumber())
                .address(addressMapper.map(home.getMortgageLenderAddress()))
                .build();
        }
        return mortgage;

    }

    private boolean getChildrenDetails(CourtNavHome home) {

        return (null != home.getChildrenApplicantResponsibility()
            || null != home.getChildrenSharedResponsibility());

    }

    private YesOrNo getAnyChildrenLivedAtAddress(CourtNavHome home) {
        return (null != home
            .getChildrenApplicantResponsibility())
            || (null != home
            .getChildrenSharedResponsibility())
            ? Yes : No;
    }


    private List<FamilyHomeEnum> getFamilyHomeDetails(CourtNavHome home) {

        List<FamilyHomeOutcomeEnum> familyHomeList = home.getWantToHappenWithFamilyHome();
        List<FamilyHomeEnum> familyHomeEnumList = new ArrayList<>();
        for (FamilyHomeOutcomeEnum familyHome : familyHomeList) {
            familyHomeEnumList.add(FamilyHomeEnum
                                       .getDisplayedValueFromEnumString(String.valueOf(familyHome)));
        }
        return familyHomeEnumList;
    }


    private List<LivingSituationEnum> getLivingSituationDetails(CourtNavHome home) {

        List<LivingSituationOutcomeEnum> livingSituationOutcomeList = home.getWantToHappenWithLivingSituation();
        List<LivingSituationEnum> livingSituationList = new ArrayList<>();
        for (LivingSituationOutcomeEnum livingSituation : livingSituationOutcomeList) {
            livingSituationList.add(LivingSituationEnum
                                        .getDisplayedValueFromEnumString(String.valueOf(livingSituation)));
        }
        return livingSituationList;

    }

    private List<MortgageNamedAfterEnum> getLandlordDetails(CourtNavHome home) {

        List<ContractEnum> contractList = home.getNamedOnRentalAgreement();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<MortgageNamedAfterEnum> getMortgageDetails(CourtNavHome home) {

        List<ContractEnum> contractList = home.getNamedOnMortgage();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<PeopleLivingAtThisAddressEnum> getPeopleLivingAtThisAddress(CourtNavHome home) {
        if (home.getCurrentlyLivesAtAddress() == null) {
            return Collections.emptyList();
        }

        return home.getCurrentlyLivesAtAddress().stream()
            .map(val -> PeopleLivingAtThisAddressEnum.getDisplayedValueFromEnumString(String.valueOf(val)))
            .toList();
    }


    private List<Element<ChildrenLiveAtAddress>> mapHomeChildren(CourtNavHome courtNavHome) {

        List<Element<ChildrenLiveAtAddress>> childList = new ArrayList<>();

        List<ChildAtAddress> childrenApplicantResponsibility = courtNavHome.getChildrenApplicantResponsibility();
        List<ChildAtAddress> childrenSharedResponsibility = courtNavHome.getChildrenSharedResponsibility();

        if (null != childrenSharedResponsibility) {
            for (ChildAtAddress child : childrenSharedResponsibility) {
                ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
                    .keepChildrenInfoConfidential(No)
                    .childFullName(child.getFullName())
                    .childsAge(String.valueOf(child.getAge()))
                    .isRespondentResponsibleForChild(Yes)
                    .build();
                childList.add(element(childrenLiveAtAddress));
            }
        }

        if (childrenApplicantResponsibility != null) {
            for (ChildAtAddress child : childrenApplicantResponsibility) {
                ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
                    .keepChildrenInfoConfidential(No)
                    .childFullName(child.getFullName())
                    .childsAge(String.valueOf(child.getAge()))
                    .isRespondentResponsibleForChild(No)
                    .build();
                childList.add(element(childrenLiveAtAddress));
            }
        }
        return childList;
    }
}
