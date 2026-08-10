package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class ApplicantConfidentialityDetails {
    @CCD(label = "First name", searchable = false)
    private final String firstName;
    @CCD(label = "Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Email", searchable = false)
    private final String email;
    @CCD(label = "Phone number", searchable = false)
    private final String phoneNumber;
    @CCD(label = "address", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;

}
