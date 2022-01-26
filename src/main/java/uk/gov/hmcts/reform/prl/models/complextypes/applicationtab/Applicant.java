package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;

import java.time.LocalDate;

@Builder
@Data
public class Applicant {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String otherGender;
    private final Address address;
    private final YesOrNo isAddressConfidential;
    private final YesOrNo isAtAddressLessThan5Years;
    private final String addressLivedLessThan5YearsDetails;
    private final YesOrNo canYouProvideEmailAddress;
    private final String email;
    private final YesOrNo isEmailAddressConfidential;
    private final String phoneNumber;
    private final YesOrNo isPhoneNumberConfidential;

    private final String representativeFirstName;
    private final String representativeLastName;
    private final String solicitorEmail;
    private final String solicitorReference;
    private final String dxNumber;
    private final Organisation solicitorOrg;



}

