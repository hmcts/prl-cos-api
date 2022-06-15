package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Organisation;

@Builder
@Data
public class FL401SolicitorDetails {
    private final String representativeFirstName;
    private final String representativeLastName;
    private final String solicitorEmail;
    private final String solicitorTelephone;
    private final String solicitorReference;
    private final String dxNumber;
    private final Organisation solicitorOrg;
}

