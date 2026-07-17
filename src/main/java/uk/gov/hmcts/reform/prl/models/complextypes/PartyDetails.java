package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnowV2;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PartyDetails {

    public static final String FULL_NAME_FORMAT = "%s %s";
    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name (if any)", searchable = false)
    private final String previousName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = " ", searchable = false)
    private final DontKnow isDateOfBirthUnknown;
    @CCD(label = "*Gender", searchable = false)
    private final Gender gender;
    @CCD(label = "*Applicant gender", searchable = false)
    private final String otherGender;
    @CCD(label = "*Place of birth (town)", searchable = false)
    private final String placeOfBirth;
    @CCD(label = " ", searchable = false)
    private final DontKnow isAddressUnknown;
    @CCD(
            label = "*Do you need to keep the address confidential?",
            hint = "If details need to be kept confidential, a C8 will be generated automatically.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isAddressConfidential;
    @CCD(
            label = "*Has applicant lived at this address for less than 5 years?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isAtAddressLessThan5Years;
    @CCD(
            label = "*Provide details of all previous addresses for the last 5 years below(if known, including the dates and starting with most recent)",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String addressLivedLessThan5YearsDetails;
    @CCD(label = "*Can you provide their email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvideEmailAddress;
    @CCD(
            label = "*Do you need to keep their email address confidential?",
            hint = "If there is confidential information, a C8 will be generated automatically.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isEmailAddressConfidential;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isPartyIdentityConfidential;
    @CCD(label = "*Home telephone number", regex = "^[0-9 +().-]{9,}$", searchable = false)
    private final String landline;
    @CCD(
            label = "*Do you need to keep their contact number confidential?",
            hint = "If details need to be kept confidential, a C8 will be generated automatically.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isPhoneNumberConfidential;
    @CCD(
            label = "*Please state their relationship to the child(ren) listed",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String relationshipToChildren;
    @CCD(label = "*Is date of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isDateOfBirthKnown;
    @CCD(label = "*Do you know their current address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isCurrentAddressKnown;
    @CCD(label = "*Can you provide a contact number?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvidePhoneNumber;
    @CCD(label = "*Is place of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPlaceOfBirthKnown;
    @CCD(label = " ", searchable = false)
    private final List<Element<OtherPersonRelationshipToChild>> otherPersonRelationshipToChildren;
    @CCD(label = "*Organisation Search", searchable = false)
    private final Organisation solicitorOrg;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address solicitorAddress;
    @CCD(label = "DX Number", searchable = false)
    private final String dxNumber;
    @CCD(label = "Solicitor reference", searchable = false)
    private final String solicitorReference;
    @CCD(label = "*Representative's first name", searchable = false)
    private final String representativeFirstName;
    @CCD(label = "*Representative's last name", searchable = false)
    private final String representativeLastName;
    @CCD(label = "*Has applicant lived at this address for less than 5 years?", searchable = false)
    private final YesNoDontKnow isAtAddressLessThan5YearsWithDontKnow;
    @CCD(label = "*Do they have legal representation?", searchable = false)
    private final YesNoDontKnow doTheyHaveLegalRepresentation;
    @CCD(
            label = "If the organisation you need isn’t listed, enter their details to send them a sign-up request\nOrganisation name",
            searchable = false
    )
    private final String sendSignUpLink;
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    private final String solicitorEmail;
    @CCD(label = "*Contact Number", regex = "^[0-9 +().-]{9,}$", searchable = false)
    private String phoneNumber;
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    private String email;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private Address address;
    @CCD(ignore = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Organisations organisations;
    @CCD(label = "*Telephone number", regex = "^[0-9 +().-]{9,}$", searchable = false)
    private final String solicitorTelephone;
    @JsonIgnore
    private final String caseTypeOfApplication;
    @CCD(label = "*Does the respondent live with the applicant? ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo respondentLivedWithApplicant;

    @CCD(label = " ", searchable = false)
    @JsonProperty("applicantPreferredContact")
    private final List<PreferredContactEnum> applicantPreferredContact;
    @CCD(label = " ", searchable = false)
    private final String applicantContactInstructions;
    @CCD(label = " ", searchable = false)
    private User user;
    @CCD(label = " ", searchable = false)
    private Response response;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo currentRespondent;

    // it will hold either applicant flag or respondent flag
    // Deprecated. kept for backward compatibility
    @CCD(label = " ", searchable = false)
    private Flags partyLevelFlag;

    @CCD(
            label = "Contact preference",
            hint = "If you do not select an option, they will be served by post if unrepresented.",
            searchable = false
    )
    private ContactPreferences contactPreferences;

    @CCD(
            label = "Remove Legal Representative Requested by Citizen",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isRemoveLegalRepresentativeRequested;

    @CCD(label = " ", typeOverride = FieldType.Text)
    private UUID partyId;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Text)
    private UUID solicitorOrgUuid;
    @CCD(label = " ", typeOverride = FieldType.Text)
    private UUID solicitorPartyId;

    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Barrister barrister;

    @JsonIgnore
    private CitizenSos citizenSosObject;

    @CCD(label = "*Do they live in a refuge?", searchable = false)
    private YesNoIDontKnowV2 liveInRefuge;
    @CCD(
            label = "*Upload a C8 form with the refuge address",
            hint = "You can download the form from www.gov.uk. The address, email address and contact number entered for this party will be kept confidential.",
            regex = ".pdf,.docx",
            categoryID = "confidential",
            searchable = false
    )
    private Document refugeConfidentialityC8Form;

    public boolean hasConfidentialInfo() {
        return this.isAddressConfidential.equals(YesOrNo.Yes)
            || this.isPhoneNumberConfidential.equals(YesOrNo.Yes);
    }

    public boolean isCanYouProvideEmailAddress() {
        return this.canYouProvideEmailAddress.equals(YesOrNo.No);
    }

    @JsonIgnore
    public boolean isEmailAddressNull() {
        if (isCanYouProvideEmailAddress()) {
            return this.isEmailAddressConfidential == YesOrNo.No;
        }
        return this.isEmailAddressConfidential == YesOrNo.Yes;
    }

    @JsonIgnore
    public String getLabelForDynamicList() {
        return String.format(
            FULL_NAME_FORMAT,
            this.firstName,
            this.lastName
        );
    }

    @JsonIgnore
    public String getRepresentativeFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.representativeFirstName,
            this.representativeLastName
        );
    }

    @JsonIgnore
    public String getRepresentativeFullNameForCaseFlags() {
        if (!StringUtils.isEmpty(this.representativeFirstName)
            && !StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                FULL_NAME_FORMAT,
                StringUtils.capitalize(this.representativeFirstName.trim()),
                StringUtils.capitalize(this.representativeLastName.trim())
            );
        } else if (!StringUtils.isEmpty(this.representativeFirstName)
            && StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                "%s",
                StringUtils.capitalize(this.representativeFirstName.trim())
            );
        } else if (StringUtils.isEmpty(this.representativeFirstName)
            && !StringUtils.isEmpty(this.representativeLastName)) {
            return String.format(
                "%s",
                StringUtils.capitalize(this.representativeLastName.trim())
            );
        } else {
            return StringUtils.EMPTY;
        }
    }

    @JsonIgnore
    public String getBarristerFullNameForCaseFlags() {
        if (getBarrister() != null && !StringUtils.isEmpty(getBarrister().getBarristerFirstName())
            && !StringUtils.isEmpty(getBarrister().getBarristerLastName())) {
            return getBarrister().getBarristerFullName();
        } else {
            return StringUtils.EMPTY;
        }
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String hintTextForC8Form;
  @CCD(label = "## Add new applicant", searchable = false, typeOverride = FieldType.Label)
  private String addNewApplicantLabel;
  @CCD(label = "**Solicitor's Details**", searchable = false, typeOverride = FieldType.Label)
  private String solicitorDetails;
  @CCD(label = "## Organisation (unregistered) (Optional)", searchable = false, typeOverride = FieldType.Label)
  private String orgUnregisteredLabel;
  @CCD(label = "## Ask an organisation to register", searchable = false, typeOverride = FieldType.Label)
  private String askOrgToRegisterLabel;
  @CCD(label = "## Add new respondent", searchable = false, typeOverride = FieldType.Label)
  private String addNewRespondentLabel;
  @CCD(label = "## Address (Optional)", searchable = false, typeOverride = FieldType.Label)
  private String addressOptionalLabel;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>All their details will be kept confidential and you do not have to complete a C8.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String refugeConfidentialC8Info;
  // ==== end synthesised definition-only fields ====
}
