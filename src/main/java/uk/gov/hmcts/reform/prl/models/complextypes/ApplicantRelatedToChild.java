package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder
public class ApplicantRelatedToChild {

    private final DynamicList selectedApplicantName;

    private final RelationshipsEnum applicantRelationWithChild;
}
