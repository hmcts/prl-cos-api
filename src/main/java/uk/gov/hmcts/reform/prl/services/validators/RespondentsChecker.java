package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RESPONDENT_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
public class RespondentsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());

        if (caseData.getCaseTypeOfApplication().equals(FL401_CASE_TYPE)) {
            PartyDetails fl401RespondentDetails = caseData.getApplicantsFL401();

            Element<PartyDetails> wrappedPartyDetails = Element.<PartyDetails>builder().value(fl401RespondentDetails).build();
            respondentsWrapped = ofNullable(Collections.singletonList(wrappedPartyDetails));
        }

        if (respondentsWrapped.isPresent() && respondentsWrapped.get().size() != 0) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails p : respondents) {
                if (!(validateMandatoryFieldsForRespondent(p, caseData))) {
                    taskErrorService.addEventError(RESPONDENT_DETAILS, RESPONDENT_DETAILS_ERROR, RESPONDENT_DETAILS_ERROR.getError());
                    return false;
                }
            }
        } else {
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

    public boolean validateMandatoryFieldsForRespondent(PartyDetails respondent, CaseData caseData) {

        List<Optional> fields = new ArrayList<>();

        fields.add(ofNullable(respondent.getFirstName()));
        fields.add(ofNullable(respondent.getLastName()));
        Optional<YesOrNo> isDateOfBirthKnown = ofNullable(respondent.getIsDateOfBirthKnown());
        fields.add(isDateOfBirthKnown);
        if (isDateOfBirthKnown.isPresent() && isDateOfBirthKnown.get().equals(Yes)) {
            fields.add(ofNullable(respondent.getDateOfBirth()));
        }
        Optional<Gender> gender = ofNullable(respondent.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(Gender.other)) {
            fields.add(ofNullable(respondent.getOtherGender()));
        }
        if (caseData.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            Optional<YesOrNo> isPlaceOfBirthKnown = ofNullable(respondent.getIsPlaceOfBirthKnown());
            fields.add(isPlaceOfBirthKnown);
            if (isPlaceOfBirthKnown.isPresent() && isPlaceOfBirthKnown.get().equals(Yes)) {
                fields.add(ofNullable(respondent.getPlaceOfBirth()));
            }
        }
        Optional<YesOrNo> isCurrentAddressKnown = ofNullable(respondent.getIsCurrentAddressKnown());
        fields.add(isCurrentAddressKnown);
        if (isCurrentAddressKnown.isPresent() && isCurrentAddressKnown.get().equals(Yes)) {
            fields.add(ofNullable(respondent.getAddress().getAddressLine1()));
        }
        Optional<YesNoDontKnow> isAtAddressLessThan5YearsWithDontKnow = ofNullable(respondent.getIsAtAddressLessThan5YearsWithDontKnow());
        fields.add(isAtAddressLessThan5YearsWithDontKnow);
        if (isAtAddressLessThan5YearsWithDontKnow.isPresent() && isAtAddressLessThan5YearsWithDontKnow.get().equals(YesNoDontKnow.yes)) {
            fields.add(ofNullable(respondent.getAddressLivedLessThan5YearsDetails()));
        }
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(respondent.getCanYouProvideEmailAddress());
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(Yes)) {
            fields.add(ofNullable(respondent.getEmail()));
        }
        Optional<YesOrNo> canYouProvidePhoneNumber = ofNullable(respondent.getCanYouProvidePhoneNumber());
        fields.add(canYouProvidePhoneNumber);
        if (canYouProvidePhoneNumber.isPresent() && canYouProvidePhoneNumber.get().equals(Yes)) {
            fields.add(ofNullable(respondent.getPhoneNumber()));
        }
        if (caseData.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            Optional<YesNoDontKnow> doTheyHaveLegalRepresentation = ofNullable(respondent.getDoTheyHaveLegalRepresentation());
            fields.add(doTheyHaveLegalRepresentation);
            if (doTheyHaveLegalRepresentation.isPresent() && doTheyHaveLegalRepresentation.get().equals(YesNoDontKnow.yes)) {
                fields.add(ofNullable(respondent.getSolicitorEmail()));
            }
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean respondentDetailsStarted(PartyDetails respondent) {

        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(respondent.getFirstName()));
        fields.add(ofNullable(respondent.getLastName()));
        fields.add(ofNullable(respondent.getDateOfBirth()));
        fields.add(ofNullable(respondent.getGender()));
        fields.add(ofNullable(respondent.getOtherGender()));
        fields.add(ofNullable(respondent.getIsDateOfBirthKnown()));
        fields.add(ofNullable(respondent.getIsPlaceOfBirthKnown()));
        fields.add(ofNullable(respondent.getIsPlaceOfBirthKnown()));
        fields.add(ofNullable(respondent.getPlaceOfBirth()));
        fields.add(ofNullable(respondent.getIsCurrentAddressKnown()));
        fields.add(ofNullable(respondent.getAddress()));
        fields.add(ofNullable(respondent.getIsAtAddressLessThan5YearsWithDontKnow()));
        fields.add(ofNullable(respondent.getAddressLivedLessThan5YearsDetails()));
        fields.add(ofNullable(respondent.getCanYouProvideEmailAddress()));
        fields.add(ofNullable(respondent.getEmail()));
        fields.add(ofNullable(respondent.getCanYouProvidePhoneNumber()));
        fields.add(ofNullable(respondent.getPhoneNumber()));
        fields.add(ofNullable(respondent.getDoTheyHaveLegalRepresentation()));
        fields.add(ofNullable(respondent.getRepresentativeFirstName()));
        fields.add(ofNullable(respondent.getRepresentativeLastName()));
        fields.add(ofNullable(respondent.getSolicitorEmail()));
        fields.add(ofNullable(respondent.getDxNumber()));
        fields.add(ofNullable(respondent.getSendSignUpLink()));

        return  fields.stream().anyMatch(Optional::isPresent);

    }

}
