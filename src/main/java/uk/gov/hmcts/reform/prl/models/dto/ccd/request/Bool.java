package uk.gov.hmcts.reform.prl.models.dto.ccd.request;

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
public class Bool {

    private Filter filter;

    private Must must;

    private List<Should> should;

    @JsonProperty("minimum_should_match")
    public int minimumShouldMatch;
}
