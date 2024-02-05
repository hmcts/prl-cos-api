package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ResponseToAllegationOfHarmDetail {
    private final Document document;
    private final YesOrNo isDocRestricted;
    private final String docRestrictedReason;

}
