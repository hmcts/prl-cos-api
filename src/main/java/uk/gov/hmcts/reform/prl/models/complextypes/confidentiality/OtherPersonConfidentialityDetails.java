package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class OtherPersonConfidentialityDetails {
    @CCD(label = "*First name(s) of the adult living with the child", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name", searchable = false)
    private final String previousName;
    @CCD(
            label = "*Give details of their relationship to (or involvement with) the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String relationshipToChildDetails;
    @CCD(label = "*Enter UK Postcode", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = "*Email", searchable = false)
    private final String email;
    @CCD(label = "*Contact Number", searchable = false)
    private final String phoneNumber;
    @CCD(
            label = "*Do you need to keep the identity of the person that the child lives with confidential? ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPersonIdentityConfidential;
    @CCD(label = "*Gender", searchable = false)
    private final Gender gender;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(
            label = "*Provide details of all previous addresses for the last 5 years below(if known, including the dates and starting with most recent)",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String addressLivedLessThan5YearsDetails;
}
