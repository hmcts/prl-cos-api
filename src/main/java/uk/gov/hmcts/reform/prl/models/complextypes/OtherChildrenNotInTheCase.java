package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildrenNotInTheCase", generate = true)
@Data
@Builder
public class OtherChildrenNotInTheCase {

    @CCD(label = "*First name(s)")
    private final String firstName;
    @CCD(label = "*Last name(s)")
    private final String lastName;
    @CCD(label = "*Date of birth", hint = "For example, 12 11 2007")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = "Is the date of birth known?", typeOverride = FieldType.YesOrNo)
    private final YesOrNo isDateOfBirthKnown;
    @CCD(label = "*Gender", typeOverride = FieldType.FixedList, typeParameterOverride = "Gender")
    private final Gender gender;
    @CCD(label = "*Child's gender")
    private final String otherGender;



}
