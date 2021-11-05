package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderAppliedFor;

import java.time.LocalDate;

@Data
@Builder
public class Child {

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final DontKnow isDateOfBirthUnknown;
    private final Gender gender;
    private final OrderAppliedFor orderAppliedFor;
    private final String relationshipToApplicant;
    private final String  relationshipToRespondent;




}
