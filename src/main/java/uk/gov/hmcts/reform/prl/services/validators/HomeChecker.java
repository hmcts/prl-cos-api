package uk.gov.hmcts.reform.prl.services.validators;

import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.APPLICANTS_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class HomeChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        Optional<Home> home =ofNullable(caseData.getHome());
        if(home.isPresent()){
            Optional<Address> address = ofNullable(home.get().getAddress());
            Optional<List<PeopleLivingAtThisAddressEnum>> peopleLivingAtThisAddress = ofNullable(home.get().getPeopleLivingAtThisAddress());
            Optional<String> textAreaSomethingElse =ofNullable(home.get().getTextAreaSomethingElse());
            Optional<YesNoBothEnum> everLivedAtTheAddress=ofNullable(home.get().getEverLivedAtTheAddress());
            Optional<YesNoBothEnum> everLivedAtTheAddressNo = ofNullable(home.get().getEverLivedAtTheAddressNo());
            Optional<YesOrNo> doAnyChildrenLiveAtAddress = ofNullable(home.get().getDoAnyChildrenLiveAtAddress());
            Optional<List<Element<ChildrenLiveAtAddress>>> doAnyChildrenLiveAtAddressYes = ofNullable(home.get().getDoAnyChildrenLiveAtAddressYes());
            Optional<YesOrNo> propertyAdaptedYesOrNo = ofNullable(home.get().getPropertyAdaptedYesOrNo());
            Optional<String> propertyAdaptedYesOrNoYes = ofNullable(home.get().getPropertyAdaptedYesOrNoYes());
            Optional<YesOrNo> mortgageOnPropertyYesOrNo = ofNullable(home.get().getMortgageOnPropertyYesOrNo());
            Optional<List<Element<Mortgage>>> mortgageOnPropertyYesOrNoIsYes = ofNullable(home.get().getMortgageOnPropertyYesOrNoIsYes());
            Optional<YesOrNo> isPropertyRentedYesNo = ofNullable(home.get().getIsPropertyRentedYesNo());
            Optional<List<Element<RentedProperty>>> isPropertyRentedYesNoIsYes = ofNullable(home.get().getIsPropertyRentedYesNoIsYes());
            Optional<YesOrNo> applicantHomeRightYesOrNo = ofNullable(home.get().getApplicantHomeRightYesOrNo());
            Optional<List<LivingSituationEnum>> livingSituation = ofNullable(home.get().getLivingSituation());
            Optional<List<FamilyHomeEnum>> familyHome = ofNullable(home.get().getFamilyHome());
            Optional<String> furtherInformation = ofNullable(home.get().getFurtherInformation());

            List<ChildrenLiveAtAddress> childrenLiveAtAddress = doAnyChildrenLiveAtAddressYes.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<Mortgage> mortgages = mortgageOnPropertyYesOrNoIsYes.get()
                .stream()
                .map(Element :: getValue)
                .collect(Collectors.toList());

            List<RentedProperty> landlords = isPropertyRentedYesNoIsYes.get()
                .stream()
                .map(Element :: getValue)
                .collect(Collectors.toList());

            for (ChildrenLiveAtAddress child : childrenLiveAtAddress) {
                Optional<YesOrNo> ischildDetailConfidential = ofNullable(child.getKeepChildrenInfoConfidential());
                ofNullable(child.getChildsAge());
                Optional<String> fullname=ofNullable(child.getChildFullName());
                Optional<YesOrNo> isRespondentResponsisbleYesNo=ofNullable(child.getIsRespondentResponsisbleYesNo());
            }

            for (Mortgage mortgage : mortgages) {
                Optional<Address> mortgageAddress = ofNullable(mortgage.getAddress());
                Optional<String> mortgageNumber = ofNullable(mortgage.getMortgageNumber());
                Optional<String> mortgageLenderName=ofNullable(mortgage.getMortgageLenderName());
                Optional<String> isRespondentResponsisbleYesNo=ofNullable(mortgage.getTextAreaSomethingElse());
            }
        }

        if (applicantsWrapped.isEmpty()) {
            return false;
        }

        boolean allFinished = true;

        taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getHome() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        boolean mandatoryCompleted = false;

        if (applicantsWrapped.isPresent() && applicantsWrapped.get().size() != 0) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails applicant : applicants) {
                mandatoryCompleted = mandatoryApplicantFieldsAreCompleted(applicant);
                if (!mandatoryCompleted) {
                    break;
                }
            }
        }
        if (mandatoryCompleted) {
            taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
            return true;
        }
        taskErrorService.addEventError(APPLICANT_DETAILS,
                                       APPLICANTS_DETAILS_ERROR,
                                       APPLICANTS_DETAILS_ERROR.getError());
        return false;
    }

    private boolean mandatoryApplicantFieldsAreCompleted(PartyDetails applicant) {


        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(applicant.getFirstName()));
        fields.add(ofNullable(applicant.getLastName()));
        fields.add(ofNullable(applicant.getDateOfBirth()));
        Optional<Gender> gender = ofNullable(applicant.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(other)) {
            fields.add(ofNullable(applicant.getOtherGender()));
        }
        fields.add(ofNullable(applicant.getPlaceOfBirth()));
        Optional<Address> address = ofNullable(applicant.getAddress());
        fields.add(address);
        if (address.isPresent() && !verifyAddressCompleted(address.get())) {
            return false;
        }
        fields.add(ofNullable(applicant.getIsAddressConfidential()));
        Optional<YesOrNo> isAtAddressLessThan5Years = ofNullable(applicant.getIsAtAddressLessThan5Years());
        fields.add(isAtAddressLessThan5Years);
        if (isAtAddressLessThan5Years.isPresent() && isAtAddressLessThan5Years.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getAddressLivedLessThan5YearsDetails()));
        }
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(applicant.getCanYouProvideEmailAddress());
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getEmail()));
            fields.add(ofNullable(applicant.getIsAddressConfidential()));
        }
        fields.add(ofNullable(applicant.getPhoneNumber()));
        fields.add(ofNullable(applicant.getIsPhoneNumberConfidential()));
        fields.add(ofNullable(applicant.getRepresentativeFirstName()));
        fields.add(ofNullable(applicant.getRepresentativeLastName()));
        fields.add(ofNullable(applicant.getSolicitorEmail()));
        Optional<Organisation> solicitorOrg = ofNullable(applicant.getSolicitorOrg());
        if (solicitorOrg.isPresent() && (solicitorOrg.get().getOrganisationID() != null)) {
            fields.add(solicitorOrg);
        } else {
            Optional<Address> solicitorAddress = ofNullable(applicant.getSolicitorAddress());
            if (solicitorAddress.isPresent() && ofNullable(solicitorAddress.get().getAddressLine1()).isEmpty()) {
                return false;
            }
            fields.add(solicitorAddress);
        }

        return fields.stream().noneMatch(Optional::isEmpty)

            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));


    }

    public boolean verifyAddressCompleted(Address address) {
        return allNonEmpty(
            address.getAddressLine1()
        );
    }

}
