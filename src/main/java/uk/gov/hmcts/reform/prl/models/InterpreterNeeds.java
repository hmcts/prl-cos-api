package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

@Data
@Builder
public class InterpreterNeeds {

    private final PartyEnum party;
    private final String name;
    private final String language;

}
