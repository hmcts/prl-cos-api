package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CREATORCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCrudPlus5RolesZisnxkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesAxrmygAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCuAccess;

@Data
@Builder(toBuilder = true)
public class Relations {

    @CCD(
            label = "Applicant",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, APPLICANTSOLICITORCrudAccess.class, CREATORCrudAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class, CitizenCrudAccess.class}
    )
    private List<Element<ChildrenAndApplicantRelation>> buffChildAndApplicantRelations;

    @CCD(
            label = "Applicant",
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, APPLICANTSOLICITORCREATORCitizenCudAccess.class, CaseworkerPrivatelawSuperuserCudAccess.class}
    )
    private List<Element<ChildrenAndApplicantRelation>> childAndApplicantRelations;

    @CCD(
            label = "Respondent",
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, APPLICANTSOLICITORCrudPlus5RolesZisnxkAccess.class}
    )
    private List<Element<ChildrenAndRespondentRelation>> buffChildAndRespondentRelations;

    @CCD(
            label = "Respondent",
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesAxrmygAccess.class, APPLICANTSOLICITORCREATORCitizenCudAccess.class, CaseworkerPrivatelawSuperuserCuAccess.class}
    )
    private List<Element<ChildrenAndRespondentRelation>> childAndRespondentRelations;

    @CCD(
            label = "Other party",
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, APPLICANTSOLICITORCrudPlus5RolesZisnxkAccess.class}
    )
    private List<Element<ChildrenAndOtherPeopleRelation>> buffChildAndOtherPeopleRelations;

    @CCD(
            label = "Other party",
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesAxrmygAccess.class, APPLICANTSOLICITORCREATORCitizenCudAccess.class, CaseworkerPrivatelawSuperuserCuAccess.class}
    )
    private List<Element<ChildrenAndOtherPeopleRelation>> childAndOtherPeopleRelations;
}
