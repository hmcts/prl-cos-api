package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudAccess;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class StatementOfService {

    @CCD(
            label = "What was served? ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavRAccess.class}
    )
    @JsonProperty("stmtOfServiceWhatWasServed")
    private StatementOfServiceWhatWasServed stmtOfServiceWhatWasServed;
    @CCD(
            label = "Recipient ",
            hint = "Do this if there are multiple recipients",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavRAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("stmtOfServiceAddRecipient")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient;
    @CCD(
            label = "Statement of Service ",
            hint = "Do this if there are multiple recipients",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavRAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("stmtOfServiceForOrder")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder;
    @CCD(
            label = "Statement of Service ",
            hint = "Do this if there are multiple recipients",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
    )
    @JsonProperty("stmtOfServiceForApplication")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForApplication;
}
