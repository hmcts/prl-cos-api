package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RentedProperty;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HOME_ERROR_ENUM;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class HomeChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<Home> home = ofNullable(caseData.getHome());
        boolean isMandatoryFilled = mandatoryHomeCompleted(home);

        if (isMandatoryFilled) {
            taskErrorService.removeError(HOME_ERROR_ENUM);
            return  isMandatoryFilled;
        }

        taskErrorService.addEventError(FL401_HOME,HOME_ERROR_ENUM, HOME_ERROR_ENUM.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getHome() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        boolean mandatoryCompleted = false;
        mandatoryCompleted = mandatoryHomeCompleted(ofNullable(caseData.getHome()));

        if (mandatoryCompleted) {
            taskErrorService.removeError(HOME_ERROR_ENUM);
            return true;
        }
        taskErrorService.addEventError(FL401_HOME,
                                       HOME_ERROR_ENUM,
                                       HOME_ERROR_ENUM.getError());
        return false;
    }

    private boolean mandatoryHomeCompleted(Optional<Home> home) {

        if (home.isPresent()) {
            List<Optional> fields = new ArrayList<>();
            fields.add(ofNullable(home.get().getAddress()));
            fields.add(ofNullable(home.get().getPeopleLivingAtThisAddress()));
            fields.add(ofNullable(home.get().getEverLivedAtTheAddress()));
            fields.add(ofNullable(home.get().getDoAnyChildrenLiveAtAddress()));
            fields.add(ofNullable(home.get().getPropertyAdaptedYesOrNo()));
            fields.add(ofNullable(home.get().getMortgageOnPropertyYesOrNo()));
            fields.add(ofNullable(home.get().getIsPropertyRentedYesNo()));
            fields.add(ofNullable(home.get().getApplicantHomeRightYesOrNo()));
            fields.add(ofNullable(home.get().getLivingSituation()));
            fields.add(ofNullable(home.get().getFamilyHome()));

            if (ofNullable(home.get().getPeopleLivingAtThisAddress()).isPresent()) {
                if (home.get().getPeopleLivingAtThisAddress().contains(PeopleLivingAtThisAddressEnum.someoneElse)) {
                    fields.add(ofNullable(home.get().getTextAreaSomethingElse()));
                }
            }

            if (ofNullable(home.get().getEverLivedAtTheAddress()).isPresent()) {
                if (home.get().getEverLivedAtTheAddress().equals(YesNoBothEnum.No)) {
                    fields.add(ofNullable(home.get().getEverLivedAtTheAddressNo()));
                }
            }

            if (ofNullable(home.get().getDoAnyChildrenLiveAtAddress()).isPresent()) {
                if (home.get().getDoAnyChildrenLiveAtAddress().equals(YesOrNo.Yes)) {
                    if (!mandatoryChildDetailsAreCompleted(ofNullable(home.get().getDoAnyChildrenLiveAtAddressYes()))) {
                        fields.add(ofNullable(null));
                    }
                }
            }

            if (ofNullable(home.get().getPropertyAdaptedYesOrNo()).isPresent()) {
                if (home.get().getPropertyAdaptedYesOrNo().equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(home.get().getPropertyAdaptedYesOrNoYes()));
                }
            }

            if (ofNullable(home.get().getMortgageOnPropertyYesOrNo()).isPresent()) {
                if (home.get().getMortgageOnPropertyYesOrNo().equals(YesOrNo.Yes)) {
                    if (!mandatoryMortgageDetailsAreCompleted(ofNullable(home.get().getMortgageOnPropertyYesOrNoIsYes()))) {
                        fields.add(ofNullable(null));
                    }
                }
            }

            if (ofNullable(home.get().getIsPropertyRentedYesNo()).isPresent()) {
                if (home.get().getIsPropertyRentedYesNo().equals(YesOrNo.Yes)) {
                    if (!mandatoryLandlordDetailsAreCompleted(ofNullable(home.get().getIsPropertyRentedYesNoIsYes()))) {
                        fields.add(ofNullable(null));
                    }
                }
            }

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""))
                && !home.get().getAddress().getAddressLine1().isBlank();
        }
        return false;
    }

    private boolean mandatoryChildDetailsAreCompleted(Optional<List<Element<ChildrenLiveAtAddress>>> doAnyChildrenLiveAtAddressYes) {

        List<ChildrenLiveAtAddress> childrenLiveAtAddress = doAnyChildrenLiveAtAddressYes.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        boolean mandatoryFields = false;
        for (ChildrenLiveAtAddress child : childrenLiveAtAddress) {
            Optional<YesOrNo> ischildDetailConfidential = ofNullable(child.getKeepChildrenInfoConfidential());
            Optional<String> age = ofNullable(child.getChildsAge());
            Optional<String> fullname = ofNullable(child.getChildFullName());
            Optional<YesOrNo> isRespondentResponsisbleYesNo = ofNullable(child.getIsRespondentResponsisbleYesNo());
            mandatoryFields = ischildDetailConfidential.isPresent() && age.isPresent()
                    && fullname.isPresent() && isRespondentResponsisbleYesNo.isPresent();
            if (!mandatoryFields) {
                return false;
            }
        }
        return mandatoryFields;
    }

    private boolean mandatoryMortgageDetailsAreCompleted(Optional<List<Element<Mortgage>>> mortgageOnPropertyYesOrNoIsYes) {

        List<Mortgage> mortgages = mortgageOnPropertyYesOrNoIsYes.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        boolean mandatoryFields = false;
        for (Mortgage mortgage : mortgages) {
            Optional<Address> mortgageAddress = ofNullable(mortgage.getAddress());
            Optional<List<MortgageNamedAfterEnum>> mortgageNamedAfter = ofNullable(mortgage.getMortgageNamedAfter());
            Optional<String> mortgageLenderName = ofNullable(mortgage.getMortgageLenderName());
            mandatoryFields = mortgageAddress.isPresent() && mortgageLenderName.isPresent()
                                && ((mortgageNamedAfter.isPresent()
                                && (mortgageNamedAfter.get().contains(MortgageNamedAfterEnum.someoneElse)
                                && !mortgage.getTextAreaSomethingElse().isBlank()))
                                || (mortgageNamedAfter.isPresent() && !mortgageNamedAfter.get().contains(MortgageNamedAfterEnum.someoneElse)));
            if (!mandatoryFields) {
                return false;
            }
        }
        return true;
    }

    private boolean mandatoryLandlordDetailsAreCompleted(Optional<List<Element<RentedProperty>>> isPropertyRentedYesNoIsYes) {

        List<RentedProperty> landlords = isPropertyRentedYesNoIsYes.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        boolean mandatoryFields = false;
        for (RentedProperty landlord : landlords) {
            Optional<String> landlordName = ofNullable(landlord.getLandlordName());
            Optional<Address> landlordAddress = ofNullable(landlord.getAddress());
            Optional<List<MortgageNamedAfterEnum>> rentalNamedAfter = ofNullable(landlord.getRentalNamedAfter());
            Optional<String> text = ofNullable(landlord.getTextAreaSomethingElse());
            mandatoryFields = landlordName.isPresent()
                    && (landlordAddress.isPresent() && verifyAddressCompleted(landlordAddress.get()))
                    && (rentalNamedAfter.isPresent()
                    && (!rentalNamedAfter.get().contains(MortgageNamedAfterEnum.someoneElse) || text.isPresent()));
            if (!mandatoryFields) {
                return false;
            }
        }
        return true;
    }

    public boolean verifyAddressCompleted(Address address) {
        return allNonEmpty(
            address.getAddressLine1()
        );
    }
}
