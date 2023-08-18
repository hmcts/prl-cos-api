package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class ChildAndOtherPeopleRelation {

    private final String otherPeopleFullName;
    private final String childFullName;
    private final String childAndOtherPeopleRelation;
    private final String childAndOtherPeopleRelationOtherDetails;
    private final YesOrNo childLivesWith;
    private final YesOrNo isChildLivesWithPersonConfidential;
}
