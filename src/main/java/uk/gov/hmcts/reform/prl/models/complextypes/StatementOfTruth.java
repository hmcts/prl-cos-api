package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FL401ConsentEnum;

import java.time.LocalDate;

@Data
@Builder
public class StatementOfTruth {
    // private final FL401ConsentEnum applicantConsent;
    private final LocalDate date;
    private final String fullname;
    private final String nameOfFirm;
    private final String signOnBehalf;

}
