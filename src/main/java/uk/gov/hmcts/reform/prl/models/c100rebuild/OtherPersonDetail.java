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
public class OtherPersonDetail {

    @JsonProperty("id")
    private String id;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("personalDetails")
    private PersonalDetails personalDetails;
    @JsonProperty("relationshipDetails")
    private RelationshipDetails relationshipDetails;
    @JsonProperty("otherPersonAddress")
    private OtherPersonAddress otherPersonAddress;

}
