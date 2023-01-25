package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder
public class OtherChildrenNotInTheCase {

    private final String firstName;
    private final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final YesOrNo isDateOfBirtKnown;
    private final Gender gender;
    private final String otherGender;



}
