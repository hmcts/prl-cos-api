package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildDetail {

    @JsonProperty("id")
    private String id;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("personalDetails")
    private PersonalDetails personalDetails;
    @JsonProperty("childMatters")
    private ChildMatters childMatters;
    @JsonProperty("parentialResponsibility")
    private ParentialResponsibility parentialResponsibility;

    @JsonProperty("liveWith")
    private List<ChildLiveWith> childLiveWith;

    @JsonProperty("mainlyLiveWith")
    private ChildLiveWith mainlyLiveWith;
}
