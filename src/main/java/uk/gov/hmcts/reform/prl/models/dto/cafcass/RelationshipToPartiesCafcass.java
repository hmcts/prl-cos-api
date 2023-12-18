package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class RelationshipToPartiesCafcass {
    private String partyId;
    private String partyFullName;
    private PartyTypeEnum partyType;
    private String childId;
    private String childFullName;
    private RelationshipsEnum relationType;
    private String otherRelationDetails;
    private YesOrNo childLivesWith;

    private String applicantFullName;
    private RelationshipsEnum childAndApplicantRelation;
    private String childAndApplicantRelationOtherDetails;
    private String applicantId;

    private String respondentFullName;
    private RelationshipsEnum childAndRespondentRelation;
    private String childAndRespondentRelationOtherDetails;
    private String respondentId;

    private String otherPeopleFullName;
    private RelationshipsEnum childAndOtherPeopleRelation;
    private String childAndOtherPeopleRelationOtherDetails;
    private YesOrNo isChildLivesWithPersonConfidential;
    private String otherPeopleId;
}
