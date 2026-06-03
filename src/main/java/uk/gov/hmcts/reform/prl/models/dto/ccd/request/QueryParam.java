package uk.gov.hmcts.reform.prl.models.dto.ccd.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class QueryParam {
    private Query query;
    private String size;
    private String from;
    private List<Sort> sort;
    @JsonProperty("search_after")
    private List<String> searchAfter;
    @JsonProperty("_source")
    private List<String> dataToReturn;
}

