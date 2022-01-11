package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PEOPLE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class OtherPeopleInTheCaseChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> othersToNotify = ofNullable(caseData.getOthersToNotify());

        if (othersToNotify.isPresent() && othersToNotify.get().size() != 0) {
            List<PartyDetails> others = caseData.getOthersToNotify()
                .stream().map(Element::getValue)
                .collect(Collectors.toList());

            boolean allFieldsCompleted = true;

            for (PartyDetails party : others) {
                allFieldsCompleted = validateMandatoryPartyDetailsForOtherPerson(party);
            }
            if (allFieldsCompleted) {
                taskErrorService.removeError(OTHER_PEOPLE_ERROR);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> othersToNotify = ofNullable(caseData.getOthersToNotify());

        if (othersToNotify.isPresent()) {
            List<PartyDetails> others = caseData.getOthersToNotify()
                .stream().map(Element::getValue)
                .collect(Collectors.toList());

            if (others.size() == 0) {
                return false;
            }
            taskErrorService.addEventError(OTHER_PEOPLE_IN_THE_CASE, OTHER_PEOPLE_ERROR, OTHER_PEOPLE_ERROR.getError());
            return others.stream().anyMatch(Objects::nonNull);
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean validateMandatoryPartyDetailsForOtherPerson(PartyDetails party) {
        boolean additionalFields = true;

        YesOrNo dob = party.getIsDateOfBirthKnown();
        if (dob != null && dob.equals(YES)) {
            additionalFields = party.getDateOfBirth() != null;
        }
        YesOrNo placeOfBirth = party.getIsPlaceOfBirthKnown();
        if (placeOfBirth != null && placeOfBirth.equals(YES)) {
            additionalFields = party.getPlaceOfBirth() != null;
        }
        YesOrNo currAdd = party.getIsCurrentAddressKnown();
        if (currAdd != null && currAdd.equals(YES)) {
            additionalFields = party.getAddress().getAddressLine1() != null;
        }
        YesOrNo canProvideEmail = party.getCanYouProvideEmailAddress();
        if (canProvideEmail != null && canProvideEmail.equals(YES)) {
            additionalFields = party.getEmail() != null;
        }
        YesOrNo canProvideTel = party.getCanYouProvidePhoneNumber();
        if (canProvideTel != null && canProvideTel.equals(YES)) {
            additionalFields = party.getPhoneNumber() != null;
        }

        List<Optional> childFields = new ArrayList<>();

        Optional<List<Element<OtherPersonRelationshipToChild>>> otherPersonRelationshipList = ofNullable(party.getPersonRelationshipWithChild());
        if (otherPersonRelationshipList.isPresent() && otherPersonRelationshipList.get().equals(Collections.emptyList())) {
            return false;
        }

        otherPersonRelationshipList.get().stream().map(Element::getValue).forEach(everyChild -> {
            childFields.add(ofNullable(everyChild.getPersonRelationshipToChild()));
        });

        childFields.add(ofNullable(party.getPersonRelationshipWithChild()));

        boolean baseFields = allNonEmpty(
            party.getFirstName(),
            party.getLastName(),
            party.getIsDateOfBirthKnown(),
            party.getIsPlaceOfBirthKnown(),
            party.getGender(),
            party.getIsCurrentAddressKnown(),
            party.getCanYouProvideEmailAddress(),
            party.getCanYouProvidePhoneNumber()
        );
        return baseFields && additionalFields && childFields.stream().anyMatch(Optional::isPresent);
    }

}
