package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class FL401Applicant {

    @CCD(label = "First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name", searchable = false)
    private final String previousName;
    @CCD(label = "Applicant's Date of birth", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "Applicant's Gender", searchable = false)
    private final String gender;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = "Do you need to keep the address confidential?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isAddressConfidential;
    @CCD(label = "Can you provide an email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvideEmailAddress;
    @CCD(label = "Email address", searchable = false)
    private final String email;
    @CCD(
            label = "Do you need to keep the email address confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isEmailAddressConfidential;
    @CCD(label = "Contact number", searchable = false)
    private final String phoneNumber;
    @CCD(
            label = "Do you need to keep the contact number confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPhoneNumberConfidential;
    @CCD(label = "Contact Preferences", searchable = false)
    private final ContactPreferences contactPreferences;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Applicant barrister",
          showCondition = "firstName=\"DO_NOT_SHOW\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String barristerLabel;
  @CCD(label = "First names", showCondition = "firstName=\"DO_NOT_SHOW\"", searchable = false)
  private String barristerFirstName;
  @CCD(label = "Last name", showCondition = "firstName=\"DO_NOT_SHOW\"", searchable = false)
  private String barristerLastName;
  @CCD(
          label = "email address",
          showCondition = "firstName=\"DO_NOT_SHOW\"",
          searchable = false,
          typeOverride = FieldType.Email
  )
  private String barristerEmail;
  @CCD(label = "Organisation", showCondition = "firstName=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.Organisation barristerOrg;
  // ==== end synthesised definition-only fields ====
}
