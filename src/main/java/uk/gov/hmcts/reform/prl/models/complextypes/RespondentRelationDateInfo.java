package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class RespondentRelationDateInfo {
    private final LocalDate applicantRelationshipDateStart;
    private final LocalDate applicantRelationshipDateEnd;
    private final LocalDate applicantRelationshipDate;
}
