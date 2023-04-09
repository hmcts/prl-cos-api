package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RespondentContactDetailsChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<CitizenDetails> citizenDetails = ofNullable(response.get().getCitizenDetails());
            if (citizenDetails.isPresent()) {
                return true;
            }
        }
        return response
            .filter(res -> anyNonEmpty(res.getCitizenDetails()
            )).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean mandatoryInfo = false;

        if (response.isPresent()) {
            Optional<CitizenDetails> citizenDetails = Optional.ofNullable(response.get()
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
            if (YesOrNo.No
                .equals(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years())) {
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
