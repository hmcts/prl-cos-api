package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class DioApplicationToApplyPermission {
    @CCD(label = " ", searchable = false)
    @JsonProperty("applyPermissionToEditSection")
    private final String applyPermissionToEditSection;

    @JsonCreator
    public DioApplicationToApplyPermission(String applyPermissionToEditSection) {
        this.applyPermissionToEditSection  = applyPermissionToEditSection;
    }

}
