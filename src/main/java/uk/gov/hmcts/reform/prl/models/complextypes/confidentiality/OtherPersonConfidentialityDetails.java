package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Builder
@Data
public class OtherPersonConfidentialityDetails {
    private final String firstName;
    private final String lastName;
    private final String previousName;
    private final String relationshipToChildDetails;
    private final Address address;
    private final String email;
    private final String phoneNumber;
    private final YesOrNo isPersonIdentityConfidential;
    private final Gender gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final String addressLivedLessThan5YearsDetails;
}
