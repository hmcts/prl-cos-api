package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorMiam {

    private final Miam respSolHaveYouAttendedMiam;
    private final Miam respSolWillingnessToAttendMiam;
}
