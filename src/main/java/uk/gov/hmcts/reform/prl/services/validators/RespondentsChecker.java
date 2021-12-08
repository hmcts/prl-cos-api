package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class RespondentsChecker implements EventChecker{

    @Override
    public boolean isFinished(CaseData caseData) {
        return caseData.getRespondents() != null;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getRespondents() != null;
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
        Optional<String> dXNumber = ofNullable(respondent.getDXNumber());
        Optional<String> sendSignUpLink = ofNullable(respondent.getSendSignUpLink());




        return false;

    }


}
