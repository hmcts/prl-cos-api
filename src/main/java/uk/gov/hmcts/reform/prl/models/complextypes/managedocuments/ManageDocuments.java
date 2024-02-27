package uk.gov.hmcts.reform.prl.models.complextypes.managedocuments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentRelatedToCase;
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
    //NOT IN USE
    private final String documentDetails;
    private final List<RestrictToCafcassHmcts> documentRestrictCheckbox;

    //PRL-4320 - manage docs redesign
    private final YesOrNo isConfidential;
    private final YesOrNo isRestricted;
    private final String restrictedDetails;
    private final List<DocumentRelatedToCase> documentRelatedToCaseCheckbox;
}
