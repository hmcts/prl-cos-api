package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveDraftOrderFields {

    //Amend Draft order
    @CCD(
            label = "Why is the order being removed?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String removeDraftOrderText;

    @CCD(
            label = "Choose the order you want to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private Object removeDraftOrdersDynamicList;
}
