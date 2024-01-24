package uk.gov.hmcts.reform.prl.models.complextypes;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class ChildrenAndApplicantRelation {

    private final String applicantFullName;
    private final String childFullName;
    private final RelationshipsEnum childAndApplicantRelation;
    private final String childAndApplicantRelationOtherDetails;
    private final YesOrNo childLivesWith;
    private final String applicantId;
    private final String childId;
}
