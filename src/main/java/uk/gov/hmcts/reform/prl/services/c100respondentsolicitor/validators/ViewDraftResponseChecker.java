package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

@Service
public class ViewDraftResponseChecker implements RespondentEventChecker {
    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        return false;
    }

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        return false;
    }
}
