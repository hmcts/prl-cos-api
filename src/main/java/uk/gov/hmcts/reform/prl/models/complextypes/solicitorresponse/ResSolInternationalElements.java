package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ResSolInternationalElements {
    private final SolicitorInternationalElement internationalElementChild;
    private final SolicitorInternationalElement internationalElementParent;
    private final SolicitorInternationalElement internationalElementJurisdiction;
    private final SolicitorInternationalElement internationalElementRequest;
}
