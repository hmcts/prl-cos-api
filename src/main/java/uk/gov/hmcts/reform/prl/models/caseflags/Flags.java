package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<FlagDetail>> details;
}
