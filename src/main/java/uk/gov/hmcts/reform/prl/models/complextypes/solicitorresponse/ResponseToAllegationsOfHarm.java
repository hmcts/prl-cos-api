package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ResponseToAllegationsOfHarm {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo responseToAllegationsOfHarmYesOrNoResponse;
    @CCD(label = " ", categoryID = "respondentC1AResponse", searchable = false)
    private final Document responseToAllegationsOfHarmDocument;
    @CCD(label = " ", searchable = false)
    private final String respondentResponseToAllegationOfHarm;
    @CCD(label = " ", categoryID = "respondentC1AResponse", searchable = false)
    private final Document responseToAllegationsOfHarmWelshDocument;
}
