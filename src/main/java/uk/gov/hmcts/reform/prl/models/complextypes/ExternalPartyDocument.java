package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
public class ExternalPartyDocument {

    private DynamicList documentCategoryList;
}
