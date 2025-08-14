package uk.gov.hmcts.reform.prl.models.c100respondentsolicitor;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

@Data
@Builder(toBuilder = true)
public class RespondentC8 {
    private ResponseDocuments respondentAc8;
    private ResponseDocuments respondentBc8;
    private ResponseDocuments respondentCc8;
    private ResponseDocuments respondentDc8;
    private ResponseDocuments respondentEc8;
}
