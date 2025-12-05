package uk.gov.hmcts.reform.prl.models.ordnancesurvey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OsPlacesResponse {
    private Header header;
    private List<Result> results;
}
