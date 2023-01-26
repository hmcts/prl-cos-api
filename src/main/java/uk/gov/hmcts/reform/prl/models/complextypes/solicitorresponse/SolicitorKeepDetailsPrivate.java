package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SolicitorKeepDetailsPrivate {

    private KeepDetailsPrivate respKeepDetailsPrivate;
    private KeepDetailsPrivate respKeepDetailsPrivateConfidentiality;

}
