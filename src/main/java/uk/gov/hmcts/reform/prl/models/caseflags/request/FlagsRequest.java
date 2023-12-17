package uk.gov.hmcts.reform.prl.models.caseflags.request;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder
public class FlagsRequest {
    List<Element<FlagDetailRequest>> details;
}
