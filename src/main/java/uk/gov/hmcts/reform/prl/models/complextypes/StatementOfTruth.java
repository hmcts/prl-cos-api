package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class StatementOfTruth {
    @JsonProperty("applicantConsent")
    private final List<FL401Consent> applicantConsent;
    @JsonProperty("signature")
    private final String signature;
    //private final SignatureEnum signatureType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate date;
    private final String fullname;
    private final String nameOfFirm;
    private final String signOnBehalf;
}
