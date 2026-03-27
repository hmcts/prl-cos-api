package uk.gov.hmcts.reform.prl.models.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PathFinderMapping {
    private String courtCode;
    private String courtName;
    private String courtField;
    private String dfjArea;
    private Boolean pathFinderEnabled;
}
