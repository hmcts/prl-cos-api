package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.CONFIRM_EDIT_CONTACT_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentContactDetailsChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response.filter(value -> ofNullable(value.getCitizenDetails())
            .filter(contact -> anyNonEmpty(
                contact.getFirstName(),
                contact.getLastName(),
                contact.getPreviousName(),
                contact.getDateOfBirth(),
                contact.getPlaceOfBirth(),
                contact.getAddress(),
                contact.getAddressHistory(),
                null != contact.getContact() ? ofNullable(contact.getContact().getEmail()) : BLANK_STRING,
                null != contact.getContact() ? ofNullable(contact.getContact().getPhoneNumber()) : BLANK_STRING
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<CitizenDetails> citizenDetails = Optional.ofNullable(response.get()
                                                                              .getCitizenDetails());
            if (citizenDetails.isPresent() && checkContactDetailsMandatoryCompleted(citizenDetails)) {
                respondentTaskErrorService.removeError(CONFIRM_EDIT_CONTACT_DETAILS_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            CONFIRM_EDIT_CONTACT_DETAILS,
            CONFIRM_EDIT_CONTACT_DETAILS_ERROR,
            CONFIRM_EDIT_CONTACT_DETAILS_ERROR.getError()
        );
        return false;
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
            if (null != citizenDetails.get().getAddressHistory()) {
                fields.add(ofNullable(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years()));
                if (YesOrNo.No
                    .equals(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years())) {
                    fields.add(ofNullable(citizenDetails.get().getAddressHistory().getPreviousAddressHistory()));
                }
            }
            if (null != citizenDetails.get().getContact()) {
                fields.add(ofNullable(citizenDetails.get().getContact().getPhoneNumber()));
                fields.add(ofNullable(citizenDetails.get().getContact().getEmail()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private boolean verifyAddressCompleted(Address address) {
        return ofNullable(address.getAddressLine1()).isPresent()
            && ofNullable(address.getPostCode()).isPresent();
    }

}
