package uk.gov.hmcts.reform.prl.models.complextypes;


import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class ChildrenAndRespondentRelation {

    @CCD(label = "Respondent's full name:")
    private final String respondentFullName;
    @CCD(label = "Child's name:")
    private final String childFullName;
    @CCD(
            label = "What is the respondent's relationship to the child?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "RelationshipsEnum"
    )
    private final RelationshipsEnum childAndRespondentRelation;
    @CCD(label = "Give details")
    private final String childAndRespondentRelationOtherDetails;
    @CCD(label = "Does the child live with this person?", typeOverride = FieldType.YesOrNo)
    private final YesOrNo childLivesWith;
    @CCD(label = " ")
    private final String respondentId;
    @CCD(label = " ")
    private final String childId;
}
