package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Data
public class FL401Respondent {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final YesOrNo isCurrentAddressKnown;
    private final Address address;
    private final YesOrNo canYouProvideEmailAddress;
    private final String email;
    private final YesOrNo canYouProvidePhoneNumber;
    private final String phoneNumber;

    private final String doTheyHaveLegalRepresentation;
    private final String representativeFirstName;
    private final String representativeLastName;
    private final String solicitorEmail;
    private final String solicitorReference;
    private final String dxNumber;
    private final Organisation solicitorOrg;
    private final ContactPreferences contactPreferences;
}

