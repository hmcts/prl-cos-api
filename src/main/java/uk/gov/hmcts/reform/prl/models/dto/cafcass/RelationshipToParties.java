package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class RelationshipToParties {

    private final String partyId;
    private final String partyFullName;

    private PartyTypeEnum partyType;

    private final String childId;
    private final String childFullName;
    private final RelationshipsEnum relationType;
    private final String otherRelationDetails;
    private final YesOrNo childLivesWith;

}
