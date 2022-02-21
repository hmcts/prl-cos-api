package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class ApplicantChild {

    private final String fullName;
    private final LocalDate dateOfBirth;
    private final String applicantChildRelationship;
    private final YesOrNo applicantRespondentShareParental;
    private final String respondentChildRelationship;

}
