package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

@Data
@Builder(toBuilder = true)
public class ChildComplex {
    private final DynamicMultiSelectList childrenlist;
}
