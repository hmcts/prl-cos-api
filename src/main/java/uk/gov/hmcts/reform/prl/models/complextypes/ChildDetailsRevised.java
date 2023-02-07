package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ChildDetailsRevised {

    private final String firstName;
    private final String lastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final String otherGender;
    private final List<OrderTypeEnum> orderAppliedFor;
    private final String parentalResponsibilityDetails;


}
