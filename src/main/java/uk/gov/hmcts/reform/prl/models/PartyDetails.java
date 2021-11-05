package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class PartyDetails {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String placeOfBirth;
    private final Address address;
    private final DontKnow isAddressUnknown;
    private final YesOrNo isAtAddressLessThan5Years;
    private final String email;
    private final String landline;
    private final String phoneNumber;
    private final String relationshipToChildren;


}
