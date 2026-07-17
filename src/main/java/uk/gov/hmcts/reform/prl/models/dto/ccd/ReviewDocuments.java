package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesAxrmygAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDocuments {

    @CCD(
            label = "Select document",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
    )
    private DynamicList reviewDocsDynamicList;
    @CCD(
            label = "Do you want to restrict access to this document?",
            hint = "Restricted documents can only be seen by court staff and the judiciary. They can be found in case file view and the confidential details tab.",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
    )
    private YesNoNotSure reviewDecisionYesOrNo;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
    )
    private String docToBeReviewed;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document reviewDoc;
    @CCD(
            label = "Document Category",
            hint = "Changing the category will change the document's location in Case File View",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private DynamicList documentCategories;
    @CCD(
            label = "Change document name",
            hint = "You should make sure not to use the same name as existing documents. Do not include the file type such as '.pdf' in the name.",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
    )
    private String documentNewName;

    //NOT IN USE
    @CCD(
            label = "Legal professional uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListConfTab;
    @CCD(
            label = "Cafcass uploaded confidential documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListConfTab;
    @CCD(
            label = "Court staff uploaded confidential documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListConfTab;
    @CCD(
            label = "Bulk scan uploaded confidential documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> bulkScannedDocListConfTab;
    //private List<Element<UploadedDocuments>> citizenUploadDocListConfTab;
    //private List<Element<UploadedDocuments>> citizenUploadedDocListDocTab;


    @CCD(
            label = "Legal professional uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab;
    @CCD(
            label = "Cafcass uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab;
    @CCD(
            label = "Local Authority uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> localAuthorityUploadDocListDocTab;
    @CCD(
            label = "Court staff uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesAxrmygAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab;
    @CCD(
            label = "Bulk scan uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> bulkScannedDocListDocTab;
    @CCD(
            label = "Citizen uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab;
    @CCD(
            label = "CourtNav uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class, CourtnavCudAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocTab;


    //PRL-4320 - manage docs redesign
    @CCD(
            label = "Restricted documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> restrictedDocuments;
    @CCD(
            label = "Confidential documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private List<Element<QuarantineLegalDoc>> confidentialDocuments;

    public static String[] reviewDocTempFields() {
        return new String[]{
            "reviewDocsDynamicList", "docToBeReviewed", "reviewDoc", "tempQuarantineDocumentList",
            "documentNewName", "documentCategories", "quarantineInformation", "docLabel"
        };
    }

    @JsonIgnore
    public List<Element<QuarantineLegalDoc>> getRemovableDocuments() {
        return Stream.of(
                legalProfUploadDocListDocTab,
                cafcassUploadDocListDocTab,
                localAuthorityUploadDocListDocTab,
                courtStaffUploadDocListDocTab,
                bulkScannedDocListDocTab,
                citizenUploadedDocListDocTab,
                courtNavUploadedDocListDocTab,
                restrictedDocuments,
                confidentialDocuments
            )
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
