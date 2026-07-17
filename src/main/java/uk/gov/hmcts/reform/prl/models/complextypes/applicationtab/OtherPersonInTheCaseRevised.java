package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class OtherPersonInTheCaseRevised {

    @CCD(label = "*First name(s)", searchable = false)
    private final String firstName;
    @CCD(label = "*Last name", searchable = false)
    private final String lastName;
    @CCD(label = "Previous name (if any)", searchable = false)
    private final String previousName;
    @CCD(label = "*Is date of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isDateOfBirthKnown;
    @CCD(label = "Date of birth", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "*Gender", searchable = false)
    private final String gender;
    @CCD(label = "*Gender", searchable = false)
    private final String otherGender;
    @CCD(label = "*Is place of birth known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPlaceOfBirthKnown;
    @CCD(label = "*Place of birth (town)", searchable = false)
    private final String placeOfBirth;
    @CCD(label = "Is current address known?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isCurrentAddressKnown;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(
            label = "Do you need to keep the address confidential?",
            showCondition = "firstName=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isAddressConfidential;
    @CCD(
            label = "*Has this person lived at this address for less than 5 years?",
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
    @CCD(label = "Can you provide email address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvideEmailAddress;
    @CCD(label = "*Email address", searchable = false)
    private final String email;
    @CCD(
            label = "Do you need to keep the email address confidential?",
            showCondition = "firstName=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isEmailAddressConfidential;
    @CCD(label = "Can you provide a contact number?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo canYouProvidePhoneNumber;
    @CCD(label = "Contact number", searchable = false)
    private final String phoneNumber;
    @CCD(
            label = "Do you need to keep the contact number confidential?",
            showCondition = "firstName=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isPhoneNumberConfidential;


}
