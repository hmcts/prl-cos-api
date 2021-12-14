package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RESPONDENT_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.OTHER;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@Service
public class RespondentsChecker implements EventChecker{

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());

        if (respondentsWrapped.isPresent() && respondentsWrapped.get().size() != 0) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails p : respondents) {
                if(!(validateMandatoryFieldsForRespondent(p))) {
                    taskErrorService.addEventError(RESPONDENT_DETAILS, RESPONDENT_DETAILS_ERROR, RESPONDENT_DETAILS_ERROR.getError());
                    return false;
                }
            }
        }
        else {
            taskErrorService.addEventError(RESPONDENT_DETAILS, RESPONDENT_DETAILS_ERROR, RESPONDENT_DETAILS_ERROR.getError());
            return false;
        }
        taskErrorService.removeError(RESPONDENT_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> respondentWrapped = ofNullable(caseData.getRespondents());

        boolean anyStarted = false;

        if (respondentWrapped.isPresent() && respondentWrapped.get().size() != 0) {
            List<PartyDetails> respondents = respondentWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails p : respondents) {
                if (respondentDetailsStarted(p)) {
                    anyStarted = true;
                }
            }
        }
        return  anyStarted;
    }


    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    private boolean validateMandatoryFieldsForRespondent(PartyDetails respondent){

        Optional<String> firstName = ofNullable(respondent.getFirstName());
        Optional<String> lastName = ofNullable(respondent.getLastName());
        Optional<LocalDate> dateOfBirth = ofNullable(respondent.getDateOfBirth());
        Optional<Gender> gender = ofNullable(respondent.getGender());
        Optional<String> otherGender = ofNullable(respondent.getOtherGender());
        Optional<YesOrNo> isDateOfBirthKnown = ofNullable(respondent.getIsDateOfBirthKnown());
        Optional<YesOrNo> isPlaceOfBirthKnown = ofNullable(respondent.getIsPlaceOfBirthKnown());
        Optional<String> placeOfBirth = ofNullable(respondent.getPlaceOfBirth());
        Optional<YesOrNo> isCurrentAddressKnown = ofNullable(respondent.getIsCurrentAddressKnown());
        Optional<Address> address = ofNullable(respondent.getAddress());
        Optional<YesNoDontKnow> isAtAddressLessThan5YearsWithDontKnow = ofNullable(respondent.getIsAtAddressLessThan5YearsWithDontKnow());
        Optional<String> addressLivedLessThan5YearsDetails = ofNullable(respondent.getAddressLivedLessThan5YearsDetails());
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(respondent.getCanYouProvideEmailAddress());
        Optional<String> email = ofNullable(respondent.getEmail());
        Optional<YesOrNo> canYouProvidePhoneNumber = ofNullable(respondent.getCanYouProvidePhoneNumber());
        Optional<String> phoneNumber = ofNullable(respondent.getPhoneNumber());
        Optional<YesNoDontKnow> doTheyHaveLegalRepresentation = ofNullable(respondent.getDoTheyHaveLegalRepresentation());
        Optional<String> representativeFirstName = ofNullable(respondent.getRepresentativeFirstName());
        Optional<String> representativeLastName = ofNullable(respondent.getRepresentativeLastName());
        Optional<String> solicitorEmail = ofNullable(respondent.getSolicitorEmail());
        Optional<String> dXNumber = ofNullable(respondent.getDxNumber());
        Optional<String> sendSignUpLink = ofNullable(respondent.getSendSignUpLink());

        List<Optional> fields = new ArrayList<>();

        fields.add(firstName);
        fields.add(lastName);
        fields.add(isDateOfBirthKnown);
        if (isDateOfBirthKnown.isPresent() && isDateOfBirthKnown.get().equals(YES)) {
            fields.add(dateOfBirth);
        }
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(OTHER)) {
            fields.add(otherGender);
        }
        fields.add(isDateOfBirthKnown);
        if (isDateOfBirthKnown.isPresent() && isPlaceOfBirthKnown.get().equals(YES)) {
            fields.add(placeOfBirth);
        }
        fields.add(isCurrentAddressKnown);
        if (isCurrentAddressKnown.isPresent() && isCurrentAddressKnown.get().equals(YES)) {
            fields.add(address);
        }
        fields.add(isAtAddressLessThan5YearsWithDontKnow);
        if (isAtAddressLessThan5YearsWithDontKnow.isPresent() && isAtAddressLessThan5YearsWithDontKnow.get().equals(YES)) {
            fields.add(addressLivedLessThan5YearsDetails);
        }
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(YES)) {
            fields.add(email);
        }
        fields.add(canYouProvidePhoneNumber);
        if (canYouProvidePhoneNumber.isPresent() && canYouProvidePhoneNumber.get().equals(YES)) {
            fields.add(phoneNumber);
        }
        fields.add(doTheyHaveLegalRepresentation);
        if (doTheyHaveLegalRepresentation.isPresent() && doTheyHaveLegalRepresentation.get().equals(YES)){
            fields.add(solicitorEmail);
        }

        boolean emptyFieldPresent = fields.stream().anyMatch(Optional::isEmpty);

        return !emptyFieldPresent;
    }

    private boolean respondentDetailsStarted(PartyDetails respondent) {
        Optional<String> firstName = ofNullable(respondent.getFirstName());
        Optional<String> lastName = ofNullable(respondent.getLastName());
        Optional<LocalDate> dateOfBirth = ofNullable(respondent.getDateOfBirth());
        Optional<Gender> gender = ofNullable(respondent.getGender());
        Optional<String> otherGender = ofNullable(respondent.getOtherGender());
        Optional<YesOrNo> isDateOfBirthKnown = ofNullable(respondent.getIsDateOfBirthKnown());
        Optional<YesOrNo> isPlaceOfBirthKnown = ofNullable(respondent.getIsPlaceOfBirthKnown());
        Optional<String> placeOfBirth = ofNullable(respondent.getPlaceOfBirth());
        Optional<YesOrNo> isCurrentAddressKnown = ofNullable(respondent.getIsCurrentAddressKnown());
        Optional<Address> address = ofNullable(respondent.getAddress());
        Optional<YesNoDontKnow> isAtAddressLessThan5YearsWithDontKnow = ofNullable(respondent.getIsAtAddressLessThan5YearsWithDontKnow());
        Optional<String> addressLivedLessThan5YearsDetails = ofNullable(respondent.getAddressLivedLessThan5YearsDetails());
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(respondent.getCanYouProvideEmailAddress());
        Optional<String> email = ofNullable(respondent.getEmail());
        Optional<YesOrNo> canYouProvidePhoneNumber = ofNullable(respondent.getCanYouProvidePhoneNumber());
        Optional<String> phoneNumber = ofNullable(respondent.getPhoneNumber());
        Optional<YesNoDontKnow> doTheyHaveLegalRepresentation = ofNullable(respondent.getDoTheyHaveLegalRepresentation());
        Optional<String> representativeFirstName = ofNullable(respondent.getRepresentativeFirstName());
        Optional<String> representativeLastName = ofNullable(respondent.getRepresentativeLastName());
        Optional<String> solicitorEmail = ofNullable(respondent.getSolicitorEmail());
        Optional<String> dXNumber = ofNullable(respondent.getDxNumber());
        Optional<String> sendSignUpLink = ofNullable(respondent.getSendSignUpLink());

        List<Optional> fields = new ArrayList<>();
        fields.add(firstName);
        fields.add(lastName);
        fields.add(dateOfBirth);
        fields.add(gender);
        fields.add(otherGender);
        fields.add(isDateOfBirthKnown);
        fields.add(dateOfBirth);
        fields.add(isPlaceOfBirthKnown);
        fields.add(placeOfBirth);
        fields.add(isCurrentAddressKnown);
        fields.add(address);
        fields.add(isAtAddressLessThan5YearsWithDontKnow);
        fields.add(addressLivedLessThan5YearsDetails);
        fields.add(canYouProvideEmailAddress);
        fields.add(email);
        fields.add(canYouProvidePhoneNumber);
        fields.add(phoneNumber);
        fields.add(doTheyHaveLegalRepresentation);
        fields.add(representativeFirstName);
        fields.add(representativeLastName);
        fields.add(phoneNumber);
        fields.add(solicitorEmail);
        fields.add(dXNumber);
        fields.add(sendSignUpLink);

        return  fields.stream().anyMatch(Optional::isPresent);

    }


}
