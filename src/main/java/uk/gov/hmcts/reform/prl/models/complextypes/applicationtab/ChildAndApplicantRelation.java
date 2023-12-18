package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class ChildAndApplicantRelation {

    private final String applicantFullName;
    private final String childFullName;
    private final String childAndApplicantRelation;
    private final String childAndApplicantRelationOtherDetails;
    private final YesOrNo childLivesWith;
}
