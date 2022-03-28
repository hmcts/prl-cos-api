package uk.gov.hmcts.reform.prl.models.court;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AreaOfLaw {

    @JsonProperty("name")
    String name;

    @JsonProperty("external_link")
    String externalLink;

    @JsonProperty("display_url")
    String displayUrl;

    @JsonProperty("external_link_desc")
    String externalDescription;
}
