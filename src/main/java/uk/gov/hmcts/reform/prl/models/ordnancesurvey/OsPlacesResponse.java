package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OsPlacesResponse {
    private List<Result> results;
}
