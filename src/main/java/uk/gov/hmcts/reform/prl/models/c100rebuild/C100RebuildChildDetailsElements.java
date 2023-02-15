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
public class C100RebuildChildDetailsElements {

    @JsonProperty("cd_childrenKnownToSocialServices")
    private String childrenKnownToSocialServices;
    @JsonProperty("cd_childrenKnownToSocialServicesDetails")
    private String childrenKnownToSocialServicesDetails;
    @JsonProperty("cd_childrenSubjectOfProtectionPlan")
    private String childrenSubjectOfProtectionPlan;
    @JsonProperty("cd_children")
    private List<ChildDetail> childDetails;
}
