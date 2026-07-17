package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationRAccess;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingTaskData {

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private String currentHearingId;
    @CCD(label = " ", searchable = false, access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class})
    private String currentHearingStatus;
}
