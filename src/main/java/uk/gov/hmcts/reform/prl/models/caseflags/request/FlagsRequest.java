package uk.gov.hmcts.reform.prl.models.caseflags.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class FlagsRequest {
    List<FlagDetailRequest> details;
}
