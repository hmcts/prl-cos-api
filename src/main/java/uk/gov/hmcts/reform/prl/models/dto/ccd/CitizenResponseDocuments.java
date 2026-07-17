package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR2CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR3CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR4CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR5CuAccess;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CitizenResponseDocuments {

    @CCD(
            label = "Respondent C8 document",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess.class}
    )
    private ResponseDocuments respondentAc8;
    @CCD(
            label = "Respondent C8 document",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, C100RESPONDENTSOLICITOR2CuAccess.class}
    )
    private ResponseDocuments respondentBc8;
    @CCD(
            label = "Respondent C8 document",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, C100RESPONDENTSOLICITOR3CuAccess.class}
    )
    private ResponseDocuments respondentCc8;
    @CCD(
            label = "Respondent C8 document",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, C100RESPONDENTSOLICITOR4CuAccess.class}
    )
    private ResponseDocuments respondentDc8;
    @CCD(
            label = "Respondent C8 document",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, C100RESPONDENTSOLICITOR5CuAccess.class}
    )
    private ResponseDocuments respondentEc8;

}
