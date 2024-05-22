package uk.gov.hmcts.reform.prl.models.complextypes;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class ChildrenAndOtherPeopleRelation {

    private final String otherPeopleFullName;
    private final String childFullName;
    private final RelationshipsEnum childAndOtherPeopleRelation;
    private final String childAndOtherPeopleRelationOtherDetails;
    private final YesOrNo childLivesWith;
    private final YesOrNo isChildLivesWithPersonConfidential;
    private final String otherPeopleId;
    private final String childId;
}
