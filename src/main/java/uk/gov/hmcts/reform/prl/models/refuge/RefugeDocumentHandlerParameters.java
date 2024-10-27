package uk.gov.hmcts.reform.prl.models.refuge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefugeDocumentHandlerParameters {
    public boolean listDocument;
    public boolean removeDocument;
    public boolean listHistoricalDocument;
    public boolean removeHistoricalDocument;
    public boolean renameDocument;
    public boolean renameDocumentBackwards;
}
