package uk.gov.hmcts.reform.prl.models.complextypes.managedocuments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ManageDocuments {

    private final DocumentPartyEnum documentParty;
    private final DynamicList documentCategories;
    private final Document document;
    private final String documentDetails;
    private final List<RestrictToCafcassHmcts> documentRestrictCheckbox;

}
