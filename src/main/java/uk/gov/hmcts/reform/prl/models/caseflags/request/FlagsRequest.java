package uk.gov.hmcts.reform.prl.models.caseflags.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlagsRequest {
    List<Element<FlagDetailRequest>> details;
}
