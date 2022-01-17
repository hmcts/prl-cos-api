package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

import java.util.List;

@Data
@Builder
public class InterpreterNeed {

    private final List<PartyEnum> party;
    private final String name;
    private final String language;
    private final String otherAssistance;

}
