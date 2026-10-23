package uk.gov.hmcts.reform.prl.models.court.factv2;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FactV2CourtSearchResult {
    private String courtId;
    private String courtName;
    private String courtSlug;
    private Double distance;
}
