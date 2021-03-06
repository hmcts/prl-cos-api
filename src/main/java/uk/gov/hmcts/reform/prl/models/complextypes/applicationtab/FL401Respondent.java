package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Builder
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
}

