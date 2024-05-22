package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PEOPLE_REVISED_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OtherPeopleInTheCaseRevisedChecker implements EventChecker {

    public final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> othersToNotify = ofNullable(caseData.getOtherPartyInTheCaseRevised());

        if (othersToNotify.isPresent()) {
            List<PartyDetails> others = caseData.getOtherPartyInTheCaseRevised()
                .stream().map(Element::getValue)
                .toList();

            if (others.isEmpty()) {
                return false;
            }

            boolean allFieldsCompleted = true;

            for (PartyDetails party : others) {
                allFieldsCompleted = validateMandatoryPartyDetailsForOtherPerson(party);
            }
            if (allFieldsCompleted) {
                taskErrorService.removeError(OTHER_PEOPLE_REVISED_ERROR);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> othersToNotify = ofNullable(caseData.getOtherPartyInTheCaseRevised());

        if (othersToNotify.isPresent()) {
            List<PartyDetails> others = caseData.getOtherPartyInTheCaseRevised()
                .stream().map(Element::getValue)
                .toList();

            boolean started = others.stream().anyMatch(Objects::nonNull);
            if (started) {
                taskErrorService.addEventError(OTHER_PEOPLE_IN_THE_CASE_REVISED, OTHER_PEOPLE_REVISED_ERROR, OTHER_PEOPLE_REVISED_ERROR.getError());
                return true;
            }
        }
        taskErrorService.removeError(OTHER_PEOPLE_REVISED_ERROR);
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean validateMandatoryPartyDetailsForOtherPerson(PartyDetails applicant) {
        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(applicant.getFirstName()));
        fields.add(ofNullable(applicant.getLastName()));
        Optional<YesOrNo> isDateOfBirthKnown = ofNullable(applicant.getIsDateOfBirthKnown());
        if (!isDateOfBirthKnown.isEmpty() && Yes.equals(isDateOfBirthKnown.get())) {
            fields.add(ofNullable(applicant.getDateOfBirth()));
        }
        Optional<Gender> gender = ofNullable(applicant.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(other)) {
            fields.add(ofNullable(applicant.getOtherGender()));
        }
        Optional<YesOrNo> isPlaceOfBirthKnown = ofNullable(applicant.getIsPlaceOfBirthKnown());
        if (!isPlaceOfBirthKnown.isEmpty() && Yes.equals(isPlaceOfBirthKnown.get())) {
            fields.add(ofNullable(applicant.getPlaceOfBirth()));
        }
        Optional<YesOrNo> isCurrentAddressKnown = ofNullable(applicant.getIsCurrentAddressKnown());
        if (!isCurrentAddressKnown.isEmpty() && Yes.equals(isCurrentAddressKnown.get())) {
            fields.add(ofNullable(applicant.getAddress()));
            fields.add(ofNullable(applicant.getIsAddressConfidential()));
        }

        Optional<YesOrNo> isAtAddressLessThan5Years = ofNullable(applicant.getIsAtAddressLessThan5Years());
        fields.add(isAtAddressLessThan5Years);
        if (isAtAddressLessThan5Years.isPresent() && isAtAddressLessThan5Years.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getAddressLivedLessThan5YearsDetails()));
        }

        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(applicant.getCanYouProvideEmailAddress());
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getEmail()));
            fields.add(ofNullable(applicant.getIsEmailAddressConfidential()));
        }
        Optional<YesOrNo> canYouProvidePhoneNumber = ofNullable(applicant.getCanYouProvidePhoneNumber());
        if (canYouProvidePhoneNumber.isPresent() && canYouProvidePhoneNumber.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getPhoneNumber()));
            fields.add(ofNullable(applicant.getIsPhoneNumberConfidential()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean verifyAddressCompleted(Address address) {
        return ofNullable(address.getAddressLine1()).isPresent()
            && ofNullable(address.getPostCode()).isPresent();
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
