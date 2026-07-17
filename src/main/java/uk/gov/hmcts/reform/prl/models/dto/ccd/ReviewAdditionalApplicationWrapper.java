package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewAdditionalApplicationWrapper {

    @CCD(
            label = "Have you reviewed the additional application?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class, CourtnavCrudAccess.class}
    )
    private YesOrNo isAdditionalApplicationReviewed;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class, CourtnavCrudAccess.class}
    )
    private AdditionalApplicationsBundle selectedAdditionalApplicationsBundle;

}
