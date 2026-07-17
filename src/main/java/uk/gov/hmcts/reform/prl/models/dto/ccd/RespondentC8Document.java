package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1CudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR2CudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR3CudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR4CudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR5CudAccess;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespondentC8Document {

    @CCD(
            label = "Respondent C8 document",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, C100RESPONDENTBARRISTER1CudAccess.class, C100RESPONDENTSOLICITOR1CudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("respondentAc8Documents")
    private List<Element<ResponseDocuments>> respondentAc8Documents;
    @CCD(
            label = "Respondent C8 document",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, C100RESPONDENTSOLICITOR2CudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("respondentBc8Documents")
    private List<Element<ResponseDocuments>> respondentBc8Documents;
    @CCD(
            label = "Respondent C8 document",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, C100RESPONDENTSOLICITOR3CudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("respondentCc8Documents")
    private List<Element<ResponseDocuments>> respondentCc8Documents;
    @CCD(
            label = "Respondent C8 document",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, C100RESPONDENTSOLICITOR4CudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("respondentDc8Documents")
    private List<Element<ResponseDocuments>> respondentDc8Documents;
    @CCD(
            label = "Respondent C8 document",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, C100RESPONDENTSOLICITOR5CudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("respondentEc8Documents")
    private List<Element<ResponseDocuments>> respondentEc8Documents;

}
