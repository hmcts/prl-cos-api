package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Consent {
    private final YesOrNo consentToTheApplication;
    private final String noConsentReason;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate applicationReceivedDate;
    private final YesOrNo permissionFromCourt;
    private final String courtOrderDetails;
}
