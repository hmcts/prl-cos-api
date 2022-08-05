package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SignatureEnum;

import java.time.LocalDate;

@Data
@Builder
@Jacksonized
public class StatementOfTruth {
    private final FL401Consent applicantConsent;
    private final SignatureEnum signature;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate date;
    private final String fullname;
    private final String nameOfFirm;
    private final String signOnBehalf;
}
