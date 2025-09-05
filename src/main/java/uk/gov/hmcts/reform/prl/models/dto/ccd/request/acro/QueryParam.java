package uk.gov.hmcts.reform.prl.models.dto.ccd.request.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Builder
public class QueryParam {
    private Query query;
    private String size;
    private String from;
    @JsonProperty("_source")
    private List<String> dataToReturn;
}

