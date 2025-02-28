package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty("address")
    private OtherPersonAddress otherPersonAddress;
    @JsonProperty("isOtherPersonAddressConfidential")
    private YesOrNo isOtherPersonAddressConfidential;
    private YesOrNo addressUnknown;
    private YesOrNo liveInRefuge;
    private Document refugeConfidentialityC8Form;


}
