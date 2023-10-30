package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HOME_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

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
            List<Optional<?>> fields = new ArrayList<>();
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

            getDetailPeopleLivingAtThisAddress(home, fields);

            getIntendToLiveAtTheAddress(home, fields);

            isChildDetailsAreCompleted(home, fields);

            isHowIsThePropertyAdaptedComplete(home, fields);

            isMandatoryMortgageDetailsAreCompleted(home, fields);

            isMandatoryLandlordDetailsAreCompleted(home, fields);

            boolean addressPresent = isAddressPresent(home);

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""))
                && addressPresent;
        }
        return false;
    }

    public boolean isAddressPresent(Optional<Home> home) {
        boolean addressPresent = false;
        if (home.isPresent()) {
            addressPresent = ofNullable(home.get().getAddress()).isPresent()
                && ofNullable(home.get().getAddress().getAddressLine1()).isPresent()
                && !home.get().getAddress().getAddressLine1().isBlank();
        }

        return addressPresent;
    }

    public void isMandatoryLandlordDetailsAreCompleted(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent()
            && ofNullable(home.get().getIsPropertyRented()).isPresent()
            && home.get().getIsPropertyRented().equals(YesOrNo.Yes)
            && !mandatoryLandlordDetailsAreCompleted(ofNullable(home.get().getLandlords()))) {
            fields.add(ofNullable(null));
        }
    }

    public void isMandatoryMortgageDetailsAreCompleted(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent()
            && ofNullable(home.get().getIsThereMortgageOnProperty()).isPresent()
            && home.get().getIsThereMortgageOnProperty().equals(YesOrNo.Yes)
            && !mandatoryMortgageDetailsAreCompleted(ofNullable(home.get().getMortgages()))) {
            fields.add(ofNullable(null));
        }
    }

    public void isHowIsThePropertyAdaptedComplete(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent()
            && ofNullable(home.get().getIsPropertyAdapted()).isPresent()
            && home.get().getIsPropertyAdapted().equals(YesOrNo.Yes)) {
            fields.add(ofNullable(home.get().getHowIsThePropertyAdapted()));
        }
    }

    public void isChildDetailsAreCompleted(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent()
            && ofNullable(home.get().getDoAnyChildrenLiveAtAddress()).isPresent()
            && home.get().getDoAnyChildrenLiveAtAddress().equals(YesOrNo.Yes)
            && !mandatoryChildDetailsAreCompleted(ofNullable(home.get().getChildren()))) {
            fields.add(ofNullable(null));
        }
    }

    public void getIntendToLiveAtTheAddress(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent()
            && ofNullable(home.get().getEverLivedAtTheAddress()).isPresent()
            && home.get().getEverLivedAtTheAddress().equals(YesNoBothEnum.No)) {
            fields.add(ofNullable(home.get().getIntendToLiveAtTheAddress()));
        }
    }

    public void getDetailPeopleLivingAtThisAddress(Optional<Home> home, List<Optional<?>> fields) {
        if (home.isPresent() && ofNullable(home.get().getPeopleLivingAtThisAddress()).isPresent()
            && home.get().getPeopleLivingAtThisAddress().contains(PeopleLivingAtThisAddressEnum.someoneElse)) {
            fields.add(ofNullable(home.get().getTextAreaSomethingElse()));
        }
    }


    public boolean mandatoryChildDetailsAreCompleted(Optional<List<Element<ChildrenLiveAtAddress>>> doAnyChildrenLiveAtAddressYes) {
        if (doAnyChildrenLiveAtAddressYes.isPresent()) {
            List<ChildrenLiveAtAddress> childrenLiveAtAddress = doAnyChildrenLiveAtAddressYes.get()
                .stream()
                .map(Element::getValue)
                .toList();
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
        return false;
    }

    private boolean mandatoryMortgageDetailsAreCompleted(Optional<Mortgage> mortgage) {
        if (mortgage.isPresent()) {
            boolean mandatoryFields = false;
            Mortgage mortgage1 = mortgage.get();
            Optional<Address> mortgageAddress = ofNullable(mortgage1.getAddress());
            Optional<List<MortgageNamedAfterEnum>> mortgageNamedAfter = ofNullable(mortgage1.getMortgageNamedAfter());
            Optional<String> mortgageLenderName = ofNullable(mortgage1.getMortgageLenderName());
            mandatoryFields = mortgageAddress.isPresent() && mortgageLenderName.isPresent()
                && ((mortgageNamedAfter.isPresent()
                && (mortgageNamedAfter.get().contains(MortgageNamedAfterEnum.someoneElse)
                && !mortgage1.getTextAreaSomethingElse().isBlank()))
                || (mortgageNamedAfter.isPresent() && !mortgageNamedAfter.get().contains(MortgageNamedAfterEnum.someoneElse)));
            return mandatoryFields;
        }
        return false;
    }

    private boolean mandatoryLandlordDetailsAreCompleted(Optional<Landlord> landlord) {
        if (landlord.isPresent()) {
            boolean mandatoryFields = false;
            Landlord landlord1 = landlord.get();
            Optional<String> landlordName = ofNullable(landlord1.getLandlordName());
            Optional<Address> landlordAddress = ofNullable(landlord1.getAddress());
            Optional<List<MortgageNamedAfterEnum>> mortgageNamedAfterList = ofNullable(landlord1.getMortgageNamedAfterList());
            Optional<String> text = ofNullable(landlord1.getTextAreaSomethingElse());
            mandatoryFields = landlordName.isPresent()
                && (landlordAddress.isPresent() && verifyAddressCompleted(landlordAddress.get()))
                && (mortgageNamedAfterList.isPresent()
                && (!mortgageNamedAfterList.get().contains(MortgageNamedAfterEnum.someoneElse) || text.isPresent()));
            return mandatoryFields;
        }
        return false;
    }

    public boolean verifyAddressCompleted(Address address) {
        return allNonEmpty(
            address.getAddressLine1()
        );
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
