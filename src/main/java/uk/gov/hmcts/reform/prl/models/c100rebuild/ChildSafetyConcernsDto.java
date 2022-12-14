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
public class ChildSafetyConcernsDto {

    @JsonProperty("physicalAbuse")
    private AbuseDTO physicalAbuse;
    @JsonProperty("psychologicalAbuse")
    private AbuseDTO psychologicalAbuse;
    @JsonProperty("emotionalAbuse")
    private AbuseDTO emotionalAbuse;
    @JsonProperty("sexualAbuse")
    private AbuseDTO sexualAbuse;
    @JsonProperty("financialAbuse")
    private AbuseDTO financialAbuse;
    @JsonProperty("somethingElse")
    private AbuseDTO somethingElse;

}
