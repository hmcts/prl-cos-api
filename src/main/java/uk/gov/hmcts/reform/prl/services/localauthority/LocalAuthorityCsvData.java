package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocalAuthorityCsvData {
    private String localAuthority;
    private String designatedFamilyCourt;
    private String specificPostCodes;
    private String excludes;
}
