package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioApplicationToApplyPermission {
    @JsonProperty("applyPermissionToEditSection")
    private final String applyPermissionToEditSection;

    @JsonCreator
    public DioApplicationToApplyPermission(String applyPermissionToEditSection) {
        this.applyPermissionToEditSection  = applyPermissionToEditSection;
    }

}
