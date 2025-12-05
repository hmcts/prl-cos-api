package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header {
    private String uri;
    private String query;
    private Integer offset;
    private Integer totalresults;
    private String format;
    private String dataset;
    private String lr;
    private Integer maxresults;
    @JsonProperty("matchprecision")
    private Double matchPrecision;
    private String epoch;
    private String lastupdate;
    @JsonProperty("output_srs")
    private String outputSrs;
}
