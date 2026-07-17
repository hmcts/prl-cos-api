package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RelationshipToChild;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherPersonInTheCase {

    @CCD(label = "*First name(s)", searchable = false)
    private String firstName;
    @CCD(label = "*Last name", searchable = false)
    private String lastName;
    @CCD(label = "Previous name (if any)", searchable = false)
    private String previousName;
    @CCD(label = "*Is date of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isDateOfBirthKnown;
    @CCD(label = "Date of birth", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @CCD(label = "*Gender", searchable = false)
    private String gender;
    @CCD(label = "*Gender", searchable = false)
    private String otherGender;
    @CCD(label = "*Is place of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isPlaceOfBirthKnown;
    @CCD(label = "*Place of birth (town)", searchable = false)
    private String placeOfBirth;
    @CCD(label = "Is current address known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isCurrentAddressKnown;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private Address address;
    @CCD(label = "Can you provide email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo canYouProvideEmailAddress;
    @CCD(label = "*Email address", searchable = false, typeOverride = FieldType.Email)
    private String email;
    @CCD(label = "Can you provide a contact number?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo canYouProvidePhoneNumber;
    @CCD(label = "Contact number", regex = "^[0-9 +().-]{9,}$", searchable = false)
    private String phoneNumber;
    @CCD(ignore = true)
    private YesOrNo isAddressConfidential;
    @CCD(ignore = true)
    private YesOrNo isEmailAddressConfidential;
    @CCD(ignore = true)
    private  YesOrNo isPhoneNumberConfidential;


  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Relationship to child", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<RelationshipToChild>> relationshipToChild;
  // ==== end synthesised definition-only fields ====
}
