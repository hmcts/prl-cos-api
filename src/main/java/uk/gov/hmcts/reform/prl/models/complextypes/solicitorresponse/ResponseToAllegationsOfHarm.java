package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ResponseToAllegationsOfHarm {

    private final YesOrNo responseToAllegationsOfHarmYesOrNoResponse;
    private final Document responseToAllegationsOfHarmDocument;
    private final String respondentResponseToAllegationOfHarm;
}
