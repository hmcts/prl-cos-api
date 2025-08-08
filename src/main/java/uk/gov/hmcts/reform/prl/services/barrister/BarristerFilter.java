package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarristerFilter {
    private String userOrgIdentifier;
    private boolean caseworkerOrSolicitor;
    private boolean caseTypeC100OrFL401;
}
