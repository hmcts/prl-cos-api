package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class Flags {

    String partyName;
    String roleOnCase;

    List<Element<FlagDetail>> details;
}
