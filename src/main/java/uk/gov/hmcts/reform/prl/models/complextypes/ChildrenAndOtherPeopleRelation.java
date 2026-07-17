package uk.gov.hmcts.reform.prl.models.complextypes;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class ChildrenAndOtherPeopleRelation {

    @CCD(label = "Full name:")
    private final String otherPeopleFullName;
    @CCD(label = "Child's name:")
    private final String childFullName;
    @CCD(
            label = "What is their relationship to the child?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "RelationshipsEnum"
    )
    private final RelationshipsEnum childAndOtherPeopleRelation;
    @CCD(label = "Give details")
    private final String childAndOtherPeopleRelationOtherDetails;
    @CCD(label = "Does the child live with this person?", typeOverride = FieldType.YesOrNo)
    private final YesOrNo childLivesWith;
    @CCD(
            label = "Do you need to keep the identity of the person that the child lives with confidential?",
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isChildLivesWithPersonConfidential;
    @CCD(label = " ")
    private final String otherPeopleId;
    @CCD(label = " ")
    private final String childId;
    @CCD(label = " ", typeOverride = FieldType.YesOrNo)
    private final YesOrNo isOtherPeopleIdConfidential;
}
