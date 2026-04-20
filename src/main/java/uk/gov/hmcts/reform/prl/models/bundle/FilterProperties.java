package uk.gov.hmcts.reform.prl.models.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterProperties {

    @JsonProperty("property")
    private String property;

    @JsonProperty("value")
    private String value;

    @JsonProperty("category")
    private String category;

    @JsonProperty("hasdraft")
    private Boolean hasdraft;
}
