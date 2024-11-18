package uk.gov.hmcts.reform.prl.models.refuge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefugeDocumentHandlerParameters {
    public boolean listDocument;
    public boolean removeDocument;
    public boolean listHistoricalDocument;
    public boolean removeHistoricalDocument;
    public boolean renameDocument;
    public boolean renameDocumentBackwards;
    public boolean onlyForApplicant;
    public boolean onlyForRespondent;
    public boolean onlyForOtherPeople;
    public boolean forAllParties;
}
