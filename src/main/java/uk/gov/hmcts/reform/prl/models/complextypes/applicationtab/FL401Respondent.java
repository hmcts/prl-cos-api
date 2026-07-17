package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class FL401Respondent {

    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name (if any)", searchable = false)
    private final String previousName;
    @CCD(label = "*Date of birth (if known)", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "*Is current address known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isCurrentAddressKnown;
    @CCD(label = "Respondent's address (if known)", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = "*Can you provide email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvideEmailAddress;
    @CCD(label = "*Email address", searchable = false)
    private final String email;
    @CCD(label = "*Can you provide a contact number?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvidePhoneNumber;
    @CCD(label = "*Contact number", searchable = false)
    private final String phoneNumber;
    @CCD(label = "*Does the respondent live with the applicant?", searchable = false, typeOverride = FieldType.Text)
    private final YesOrNo isRespondentLiveWithApplicant;

    @CCD(label = "*Do they have legal representation?", searchable = false)
    private final String doTheyHaveLegalRepresentation;
    @CCD(label = "Representative's first name", searchable = false)
    private final String representativeFirstName;
    @CCD(label = "Representative's last name", searchable = false)
    private final String representativeLastName;
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    private final String solicitorEmail;
    @CCD(label = "Solicitor reference", searchable = false)
    private final String solicitorReference;
    @CCD(label = "DX Number", searchable = false)
    private final String dxNumber;
    @CCD(label = "*Organisation Search", searchable = false)
    private final Organisation solicitorOrg;
    @CCD(label = "Contact Preferences", searchable = false)
    private final ContactPreferences contactPreferences;

    @CCD(label = "First names", searchable = false)
    private final String barristerFirstName;
    @CCD(label = "Last name", searchable = false)
    private final String barristerLastName;
    @CCD(label = "email address", searchable = false, typeOverride = FieldType.Email)
    private final String barristerEmail;
    @CCD(label = "Organisation", searchable = false)
    private final Organisation barristerOrg;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Respondent Solicitor",
          showCondition = "doTheyHaveLegalRepresentation=\"yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String respondentSolicitorLabel;
  @CCD(
          label = "### Respondent barrister",
          showCondition = "barristerFirstName!=\"\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String barristerLabel;
  // ==== end synthesised definition-only fields ====
}

