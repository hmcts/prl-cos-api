package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;

import java.time.LocalDate;

@Data
@Builder
public class StatementOfTruth {
    private final FL401Consent applicantConsent;
    private final LocalDate date;
    private final String fullname;
    private final String nameOfFirm;
    private final String signOnBehalf;

    @JsonCreator
    public StatementOfTruth(FL401Consent applicantConsent, LocalDate date, String fullname,
                            String nameOfFirm, String signOnBehalf) {
        this.applicantConsent = applicantConsent;
        this.date = date;
        this.fullname = fullname;
        this.nameOfFirm = nameOfFirm;
        this.signOnBehalf = signOnBehalf;
    }

}
