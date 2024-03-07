package uk.gov.hmcts.reform.prl.models.complextypes;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class ChildrenAndRespondentRelation {

    private final String respondentFullName;
    private final String childFullName;
    private final RelationshipsEnum childAndRespondentRelation;
    private final String childAndRespondentRelationOtherDetails;
    private final YesOrNo childLivesWith;
    private final String respondentId;
    private final String childId;
}
