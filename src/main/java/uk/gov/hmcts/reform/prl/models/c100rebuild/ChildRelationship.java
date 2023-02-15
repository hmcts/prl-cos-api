package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChildRelationship {

    @JsonProperty("childId")
    private String childId;
    @JsonProperty("relationshipType")
    private String relationshipType;
    @JsonProperty("otherRelationshipTypeDetails")
    private String otherRelationshipTypeDetails;

}
