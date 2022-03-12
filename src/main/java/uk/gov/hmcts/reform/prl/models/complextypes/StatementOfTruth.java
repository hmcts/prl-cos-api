package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Data
@Builder
@Jacksonized
public class StatementOfTruth {
    private final LocalDate date;
    private final String fullname;
    private final String nameOfFirm;
    private final String signOnBehalf;
}
