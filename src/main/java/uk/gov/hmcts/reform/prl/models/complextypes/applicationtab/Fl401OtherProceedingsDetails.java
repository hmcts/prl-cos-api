package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class Fl401OtherProceedingsDetails {
    @CCD(label = "Name of court", searchable = false)
    private final String nameOfCourt;
    @CCD(label = "Case number (optional)", searchable = false)
    private final String caseNumber;
    @CCD(label = "Type of case", searchable = false)
    private final String typeOfCase;
    @CCD(label = "Any other details", searchable = false)
    private final String anyOtherDetails;
}
