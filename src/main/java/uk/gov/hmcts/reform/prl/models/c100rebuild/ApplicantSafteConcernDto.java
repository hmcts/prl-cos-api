package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicantSafteConcernDto {

    @JsonProperty("physicalAbuse")
    private AbuseDto physicalAbuse;
    @JsonProperty("psychologicalAbuse")
    private AbuseDto psychologicalAbuse;
    @JsonProperty("emotionalAbuse")
    private AbuseDto emotionalAbuse;
    @JsonProperty("sexualAbuse")
    private AbuseDto sexualAbuse;
    @JsonProperty("financialAbuse")
    private AbuseDto financialAbuse;
    @JsonProperty("somethingElse")
    private AbuseDto somethingElse;

}
