package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Data
@Builder
@Jacksonized
public class RespondentRelationDateInfo {
    private final LocalDate applicantRelationshipDateStart;
    private final LocalDate applicantRelationshipDateEnd;
    private final LocalDate applicantRelationshipDate;
}
