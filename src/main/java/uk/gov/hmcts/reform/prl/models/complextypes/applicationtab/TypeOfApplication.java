package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class TypeOfApplication {

    @CCD(label = "Orders applied for", searchable = false)
    private final String ordersApplyingFor;
    @CCD(label = "Type of child arrangements order", searchable = false)
    private final String typeOfChildArrangementsOrder;
    @CCD(label = "Provide more information", searchable = false, typeOverride = FieldType.TextArea)
    private final String natureOfOrder;
    @CCD(label = "Give details of why permission is required from the court.", searchable = false)
    private final String applicationPermissionRequiredReason;
    @CCD(label = "Have you applied to the court for permission to make this application?", searchable = false)
    private final String applicationPermissionRequired;
    @CCD(
            label = "Is there an order under section 91(14) Children Act 1989, a limited civil restraint order, a general civil restraint order or an extended civil restraint order in force which means you need permission to make this application?",
            searchable = false
    )
    private final String orderInPlacePermissionRequired;
    @CCD(label = "Provide case number and name of the court", searchable = false)
    private final String orderDetailsForPermissions;
    @CCD(label = "Uploaded Order", searchable = false)
    private final String uploadOrderDocForPermission;

}
