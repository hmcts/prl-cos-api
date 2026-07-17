package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRPlus6RolesXmoyviAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCrudPlus11RolesXtukayAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewRaRequestWrapper {
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess.class, CitizenCruAccess.class}
    )
    private YesOrNo isCaseFlagsTaskCreated;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {APPLICANTSOLICITORCrudPlus11RolesXtukayAccess.class}
    )
    private List<Element<Flags>> selectedFlags;
}
