package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class OtherPeopleInTheCaseChecker implements EventChecker {


    @Override
    public boolean isFinished(CaseData caseData) {
        if (caseData.getOthersToNotify() != null) {
            List<PartyDetails> others = caseData.getOthersToNotify()
                .stream().map(Element::getValue)
                .collect(Collectors.toList());

            boolean allFieldsCompleted = true;

            for (PartyDetails party : others) {
                allFieldsCompleted = hasMandatoryCompleted(caseData);
            }
            return allFieldsCompleted;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        if (caseData.getOthersToNotify() != null) {
            List<PartyDetails> others = caseData.getOthersToNotify()
                .stream().map(Element::getValue)
                .collect(Collectors.toList());

            if (others.size() == 0) {
                return false;
            }

            return others.stream().map(PartyDetails::getFirstName).anyMatch(m -> !m.isEmpty());
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        if (caseData.getOthersToNotify() != null) {
            List<PartyDetails> others = caseData.getOthersToNotify()
                .stream().map(Element::getValue)
                .collect(Collectors.toList());

            boolean allMandatoryCompleted = true;

            for (PartyDetails party:others){
                allMandatoryCompleted = validateMandatoryPartyDetailsForOtherPerson(party);
            }
            return allMandatoryCompleted;
        }
        return false;
    }


    public boolean validateMandatoryPartyDetailsForOtherPerson(PartyDetails party) {
        boolean baseFields = allNonEmpty(
                                party.getFirstName(),
                                party.getLastName(),
                                party.getIsDateOfBirthKnown(),
                                party.getGender(),
                                party.getIsCurrentAddressKnown(),
                                party.getCanYouProvideEmailAddress(),
                                party.getCanYouProvidePhoneNumber()
        );
        boolean additionalFields = true;

        YesOrNo dob = party.getIsDateOfBirthKnown();
        YesOrNo currAdd = party.getIsCurrentAddressKnown();
        YesOrNo canProvideEmail = party.getCanYouProvideEmailAddress();
        YesOrNo canProvideTel = party.getCanYouProvidePhoneNumber();

        if (dob != null && dob.equals(YES) ) {
            additionalFields = party.getDateOfBirth() != null;
        }
        if (currAdd != null && currAdd.equals(YES)) {
            additionalFields = party.getAddress().getAddressLine1() != null;
        }
        if (canProvideEmail != null && canProvideEmail.equals(YES)) {
            additionalFields = party.getEmail() != null;
        }
        if (canProvideTel != null && canProvideTel.equals(YES)) {
            additionalFields = party.getPhoneNumber() != null;
        }
        return baseFields && additionalFields;
    }

}
