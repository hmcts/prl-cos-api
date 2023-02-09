package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RespondentContactDetailsChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return activeRespondent.filter(partyDetailsElement -> anyNonEmpty(partyDetailsElement
                                                                              .getValue()
                                                                              .getResponse()
                                                                              .getCitizenDetails()
        )).isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        boolean mandatoryInfo = false;
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        if (activeRespondent.isPresent()) {
            Optional<CitizenDetails> citizenDetails = Optional.ofNullable(activeRespondent.get()
                                                                              .getValue()
                                                                              .getResponse()
                                                                              .getCitizenDetails());
            if (!citizenDetails.isEmpty() && checkContactDetailsMandatoryCompleted(citizenDetails)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkContactDetailsMandatoryCompleted(Optional<CitizenDetails> citizenDetails) {
        List<Optional<?>> fields = new ArrayList<>();
        if (citizenDetails.isPresent()) {
            fields.add(ofNullable(citizenDetails.get().getFirstName()));
            fields.add(ofNullable(citizenDetails.get().getLastName()));
            fields.add(ofNullable(citizenDetails.get().getDateOfBirth()));
            Optional<Address> address = ofNullable(citizenDetails.get().getAddress());
            fields.add(address);
            if (address.isPresent() && !verifyAddressCompleted(address.get())) {
                return false;
            }
            fields.add(ofNullable(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years()));
            if (citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years()
                .equals(YesOrNo.No)) {
                fields.add(ofNullable(citizenDetails.get().getAddressHistory().getPreviousAddressHistory()));
            }
            fields.add(ofNullable(citizenDetails.get().getContact().getPhoneNumber()));
            fields.add(ofNullable(citizenDetails.get().getContact().getEmail()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private boolean verifyAddressCompleted(Address address) {
        return ofNullable(address.getAddressLine1()).isPresent()
            && ofNullable(address.getPostCode()).isPresent();
    }

}
