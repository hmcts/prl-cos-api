package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ResSolInternationalElements {
    private final SolicitorInternationalElement internationalElementChildInfo;
    private final SolicitorInternationalElement internationalElementParentInfo;
    private final SolicitorInternationalElement internationalElementJurisdictionInfo;
    private final SolicitorInternationalElement internationalElementRequestInfo;
}
