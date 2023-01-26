package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentInterpreterNeeds {

    private final List<PartyEnum> party;
    private final String relationName;
    private final String requiredLanguage;
    private final String respondentOtherAssistance;
}
