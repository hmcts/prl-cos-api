package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class CaseManagementLocation {
    @CCD(label = " ", searchable = false)
    private final String region;
    @CCD(label = " ", searchable = false)
    private final String regionId;
    @CCD(label = " ", searchable = false)
    private final String baseLocation;
    @CCD(label = " ", searchable = false)
    private final String baseLocationId;
    @CCD(label = " ", searchable = false)
    private final String regionName;
    @CCD(label = " ", searchable = false)
    private final String baseLocationName;
}
