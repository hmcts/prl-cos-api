package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Data
public class FL401Applicant {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final String gender;
    private final Address address;
    private final YesOrNo isAddressConfidential;
    private final YesOrNo canYouProvideEmailAddress;
    private final String email;
    private final YesOrNo isEmailAddressConfidential;
    private final String phoneNumber;
    private final YesOrNo isPhoneNumberConfidential;
}
