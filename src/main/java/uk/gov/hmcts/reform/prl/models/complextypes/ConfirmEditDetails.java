package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ConfirmEditDetails {
    private final String firstName;
    private final String lastName;
    private final String previousNames;
    private final LocalDate dateOfBirth;
    private final String placeOfBirth;
    private final Address address;
    private final YesOrNo haveYouLivedAtAddress;
    private final List<Element<AddressCollection>> addressDetails;
    private final String telephoneNumber;
    private final String emailAddress;
}
