package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class ChildAndRespondentRelation {

    private final String respondentFullName;
    private final String childFullName;
    private final String childAndRespondentRelation;
    private final String childAndRespondentRelationOtherDetails;
    private final YesOrNo childLivesWith;
}
