package uk.gov.hmcts.reform.prl.models.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.DfjArea;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrlCourtMapping {
    private String courtCode;
    private String courtName;
    private DfjArea dfjArea;
}
