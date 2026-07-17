package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ApplicantConfidentialityDetails {
    @CCD(label = "First name", searchable = false)
    private String firstName;
    @CCD(label = "Last name", searchable = false)
    private String lastName;
    @CCD(label = "Email", searchable = false)
    private String email;
    @CCD(label = "Phone number", searchable = false)
    private String phoneNumber;
    @CCD(label = "address", searchable = false, typeOverride = FieldType.AddressUK)
    private Address address;

}
