package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.documents.CorrespondenceDocument;

import java.util.List;

@Data
@Builder
public class ChildrenLiveAtAddress {
    private final YesOrNo keepChildrenInfoConfidential;
    private final String childFullName;
    private final String childAge;
    private final String personResponsible;
}
