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
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HOME_ERROR;
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
            taskErrorService.removeError(HOME_ERROR);
            return  isMandatoryFilled;
        }

        taskErrorService.addEventError(FL401_HOME,HOME_ERROR, HOME_ERROR.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getHome() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        boolean mandatoryCompleted = false;
        mandatoryCompleted = mandatoryHomeCompleted(ofNullable(caseData.getHome()));

        if (mandatoryCompleted) {
            taskErrorService.removeError(HOME_ERROR);
            return true;
        }
        taskErrorService.addEventError(FL401_HOME,
                                       HOME_ERROR,
                                       HOME_ERROR.getError());
        return false;
    }

    private boolean mandatoryHomeCompleted(Optional<Home> home) {

        if (home.isPresent()) {
            List<Optional> fields = new ArrayList<>();
            fields.add(ofNullable(home.get().getAddress()));
            fields.add(ofNullable(home.get().getPeopleLivingAtThisAddress()));
            fields.add(ofNullable(home.get().getEverLivedAtTheAddress()));
            fields.add(ofNullable(home.get().getDoAnyChildrenLiveAtAddress()));
            fields.add(ofNullable(home.get().getIsPropertyAdapted()));
            fields.add(ofNullable(home.get().getIsThereMortgageOnProperty()));
            fields.add(ofNullable(home.get().getIsPropertyRented()));
            fields.add(ofNullable(home.get().getDoesApplicantHaveHomeRights()));
            fields.add(ofNullable(home.get().getLivingSituation()));
            fields.add(ofNullable(home.get().getFamilyHome()));

            if (ofNullable(home.get().getPeopleLivingAtThisAddress()).isPresent()) {
                if (home.get().getPeopleLivingAtThisAddress().contains(PeopleLivingAtThisAddressEnum.someoneElse)) {
                    fields.add(ofNullable(home.get().getTextAreaSomethingElse()));
                }
            }

            if (ofNullable(home.get().getEverLivedAtTheAddress()).isPresent()) {
                if (home.get().getEverLivedAtTheAddress().equals(YesNoBothEnum.No)) {
                    fields.add(ofNullable(home.get().getIntendToLiveAtTheAddress()));
                }
            }

            if (ofNullable(home.get().getDoAnyChildrenLiveAtAddress()).isPresent()) {
                if (home.get().getDoAnyChildrenLiveAtAddress().equals(YesOrNo.Yes)) {
                    if (!mandatoryChildDetailsAreCompleted(ofNullable(home.get().getChildren()))) {
                        fields.add(ofNullable(null));
                    }
                }
            }

            if (ofNullable(home.get().getIsPropertyAdapted()).isPresent()) {
                if (home.get().getIsPropertyAdapted().equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(home.get().getHowIsThePropertyAdapted()));
                }
            }

            if (ofNullable(home.get().getIsThereMortgageOnProperty()).isPresent()) {
                if (home.get().getIsThereMortgageOnProperty().equals(YesOrNo.Yes)) {
                    if (!mandatoryMortgageDetailsAreCompleted(ofNullable(home.get().getMortgages()))) {
                        fields.add(ofNullable(null));
                    }
                }
            }

            if (ofNullable(home.get().getIsPropertyRented()).isPresent()) {
                if (home.get().getIsPropertyRented().equals(YesOrNo.Yes)) {
                    if (!mandatoryLandlordDetailsAreCompleted(ofNullable(home.get().getLandlords()))) {
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
            Optional<YesOrNo> isRespondentResponsisbleYesNo = ofNullable(child.getIsRespondentResponsibleForChild());
            mandatoryFields = ischildDetailConfidential.isPresent() && age.isPresent()
                    && fullname.isPresent() && isRespondentResponsisbleYesNo.isPresent();
            if (!mandatoryFields) {
                return false;
            }
        }
        return mandatoryFields;
    }

    private boolean mandatoryMortgageDetailsAreCompleted(Optional<List<Element<Mortgage>>> mortgagesList) {

        List<Mortgage> mortgages = mortgagesList.get()
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

    private boolean mandatoryLandlordDetailsAreCompleted(Optional<List<Element<Landlord>>> isPropertyRentedYesNoIsYes) {

        List<Landlord> landlords = isPropertyRentedYesNoIsYes.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        boolean mandatoryFields = false;
        for (Landlord landlord : landlords) {
            Optional<String> landlordName = ofNullable(landlord.getLandlordName());
            Optional<Address> landlordAddress = ofNullable(landlord.getAddress());
            Optional<List<MortgageNamedAfterEnum>> mortgageNamedAfterList = ofNullable(landlord.getMortgageNamedAfterList());
            Optional<String> text = ofNullable(landlord.getTextAreaSomethingElse());
            mandatoryFields = landlordName.isPresent()
                    && (landlordAddress.isPresent() && verifyAddressCompleted(landlordAddress.get()))
                    && (mortgageNamedAfterList.isPresent()
                    && (!mortgageNamedAfterList.get().contains(MortgageNamedAfterEnum.someoneElse) || text.isPresent()));
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
