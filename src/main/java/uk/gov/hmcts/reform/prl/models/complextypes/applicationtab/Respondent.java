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
public class Respondent {

    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name", searchable = false)
    private final String previousName;
    @CCD(label = "*Date of birth", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "*Gender", searchable = false)
    private final String gender;
    @CCD(label = "*Gender", searchable = false)
    private final String otherGender;
    @CCD(label = "*Is place of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPlaceOfBirthKnown;
    @CCD(label = "*Place of birth", searchable = false)
    private final String placeOfBirth;
    @CCD(label = "*Is current address known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isCurrentAddressKnown;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = "*Has respondent lived at this address for less than 5 years?", searchable = false)
    private final String isAtAddressLessThan5YearsWithDontKnow;
    @CCD(
            label = "*Provide details of all previous addresses for the last 5 years below(if known, including the dates and starting with most recent)",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String addressLivedLessThan5YearsDetails;
    @CCD(label = "*Can you provide email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvideEmailAddress;
    @CCD(label = "*Email address", searchable = false)
    private final String email;
    @CCD(label = "*Can you provide a contact number?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvidePhoneNumber;
    @CCD(label = "*Contact number", searchable = false)
    private final String phoneNumber;

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

    @CCD(label = "Do you need to keep the address confidential?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isAddressConfidential;
    @CCD(
            label = "Do you need to keep the email address confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isEmailAddressConfidential;
    @CCD(
            label = "Do you need to keep the contact number confidential?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPhoneNumberConfidential;
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
          showCondition = "doTheyHaveLegalRepresentation=\"Yes\"",
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

