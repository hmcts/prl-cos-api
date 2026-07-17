package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentRemovalConfirmOption;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRemovalWrapper {
    @CCD(
            label = "Case documents",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private DynamicList documentRemovalCaseDocuments;
    @CCD(label = "Document to remove", searchable = false, access = {CaseworkerPrivatelawSuperuserCrudAccess.class})
    private Document documentRemovalDocumentToRemove;
    @CCD(
            label = "Confirm",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentRemovalConfirmOptions",
            access = {CaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private List<DocumentRemovalConfirmOption> documentRemovalConfirmOptions;
}
