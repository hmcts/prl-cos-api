package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PartyDetails {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final String otherGender;
    private final String placeOfBirth;
    private final Address address;
    private final DontKnow isAddressUnknown;
    private final YesOrNo isAddressConfidential;
    private final YesOrNo isAtAddressLessThan5Years;
    private final String addressLivedLessThan5YearsDetails;
    private final YesOrNo canYouProvideEmailAddress;
    private final String email;
    private final YesOrNo isEmailAddressConfidential;
    private final String landline;
    private final String phoneNumber;
    private final YesOrNo isPhoneNumberConfidential;
    private final String relationshipToChildren;
    private final YesOrNo isDateOfBirthKnown;
    private final YesOrNo isCurrentAddressKnown;
    private final YesOrNo canYouProvidePhoneNumber;
    private final YesOrNo isPlaceOfBirthKnown;
    private final List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildren;
    private final Organisation solicitorOrg;
    private final Address solicitorAddress;
    private final String dxNumber;
    private final String solicitorReference;
    private final String representativeFirstName;
    private final String representativeLastName;
    private final YesNoDontKnow isAtAddressLessThan5YearsWithDontKnow;
    private final YesNoDontKnow doTheyHaveLegalRepresentation;
    private final String sendSignUpLink;
    private final String solicitorEmail;
    private final String caseTypeOfApplication;





}
