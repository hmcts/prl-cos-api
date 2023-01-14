package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbuseDto {

    @JsonProperty("behaviourDetails")
    private String behaviourDetails;
    @JsonProperty("behaviourStartDate")
    private String behaviourStartDate;
    @JsonProperty("isOngoingBehaviour")
    private YesOrNo isOngoingBehaviour;
    @JsonProperty("seekHelpFromPersonOrAgency")
    private YesOrNo seekHelpFromPersonOrAgency;
    @JsonProperty("seekHelpDetails")
    private String seekHelpDetails;
    @JsonProperty("childrenConcernedAbout")
    private String [] childrenConcernedAbout;
}
