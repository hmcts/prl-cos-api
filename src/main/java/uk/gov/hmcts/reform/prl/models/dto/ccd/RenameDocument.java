package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenameDocument {

    @CCD(
            label = "Change document name",
            hint = "You should make sure not to use the same name as existing documents. Do not include the file type such as '.pdf' in the name.",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
    )
    private String newNameForDocument;

    @CCD(
            label = "Documents",
            hint = "Select document",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("renameDocumentsList")
    private DynamicList renameDocumentsList;

    @CCD(
            label = "Document Category",
            hint = "Changing the category will change the document's location in Case File View",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("categoryDocumentsList")
    private DynamicList categoryDocumentsList;

    @CCD(
            label = "Document selected :",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("renameListDocSelected")
    private String renameListDocSelected;

}
