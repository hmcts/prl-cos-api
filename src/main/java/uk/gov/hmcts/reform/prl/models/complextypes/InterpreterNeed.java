package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

@Data
@Builder
public class InterpreterNeed {

    private final PartyEnum party;
    private final String name;
    private final String language;

}
