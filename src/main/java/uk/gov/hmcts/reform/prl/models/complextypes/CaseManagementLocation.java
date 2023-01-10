package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CaseManagementLocation {
    private final String region;
    private final String baseLocation;
}
