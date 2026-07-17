package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus3RolesIslhzwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOCIALWORKERRPlus11RolesVgmmitAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCrudPlus5RolesYfhrueAccess;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentManagementDetails {

    @CCD(
            label = "test",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesIslhzwAccess.class}
    )
    @JsonProperty("isC8DocumentPresent")
    private String isC8DocumentPresent;

    @CCD(
            label = "Legal professional uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    @JsonProperty("legalProfQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList;
    @CCD(
            label = "Court staff uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    @JsonProperty("courtStaffQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsList;
    @CCD(
            label = "Cafcass uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    @JsonProperty("cafcassQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList;
    @CCD(
            label = "Local Authority uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    @JsonProperty("localAuthorityQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> localAuthorityQuarantineDocsList;
    @CCD(
            label = "Citizen uploaded quarantine documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("citizenQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> citizenQuarantineDocsList;
    @CCD(
            label = "Temparary QuarantineList",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassCitizenRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("tempQuarantineDocumentList")
    List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList;

    @CCD(
            label = "CourtNav uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class, CourtnavCudAccess.class}
    )
    @JsonProperty("courtNavQuarantineDocumentList")
    List<Element<QuarantineLegalDoc>> courtNavQuarantineDocumentList;

    //NOT IN USE
    @CCD(
            label = "Citizen uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("citizenUploadQuarantineDocsList")
    private List<Element<UploadedDocuments>> citizenUploadQuarantineDocsList;

    //PRL-3562 - manage document enhancements
    @CCD(
            label = "Add a document",
            hint = "Upload a file to the system",
            searchable = false,
            access = {LASOCIALWORKERRPlus11RolesVgmmitAccess.class}
    )
    @JsonProperty("manageDocuments")
    private List<Element<ManageDocuments>> manageDocuments;
    @CCD(
            label = "test",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawBulkscanCrudPlus5RolesYfhrueAccess.class}
    )
    private String manageDocumentsTriggeredBy;
    @CCD(
            label = "test",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawBulkscanCrudPlus5RolesYfhrueAccess.class}
    )
    private String manageDocumentsRestrictedFlag;

    @JsonIgnore
    public List<Element<QuarantineLegalDoc>> getRemovableDocuments() {
        return Stream.of(
            legalProfQuarantineDocsList,
            courtStaffQuarantineDocsList,
            cafcassQuarantineDocsList,
            localAuthorityQuarantineDocsList,
            citizenQuarantineDocsList,
            courtNavQuarantineDocumentList
        )
          .filter(Objects::nonNull)
          .flatMap(List::stream)
          .toList();
    }

}
