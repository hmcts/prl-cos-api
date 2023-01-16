package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CaseManagementLocation {
    private final String regionId;
    private final String baseLocationId;
    private final String regionName;
    private final String baseLocationName;
}
