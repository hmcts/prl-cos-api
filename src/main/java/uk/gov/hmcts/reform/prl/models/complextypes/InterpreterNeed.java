package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

import java.util.List;

@Data
@Builder
public class InterpreterNeed {

    private List<PartyEnum> party;
    private String name;
    private String language;
    private String otherAssistance;

}
