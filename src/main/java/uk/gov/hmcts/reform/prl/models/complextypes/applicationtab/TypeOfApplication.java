package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TypeOfApplication {

    private final String ordersApplyingFor;
    private final String typeOfChildArrangementsOrder;
    private final String natureOfOrder;
    private final String applicationPermissionRequiredReason;
    private final String applicationPermissionRequired;
    private final String orderInPlacePermissionRequired;
    private final String orderDetailsForPermissions;
    private final String uploadOrderDocForPermission;

}
