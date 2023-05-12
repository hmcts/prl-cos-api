package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public interface RespondentEventChecker {

    boolean isStarted(PartyDetails respondingParty);

    boolean isFinished(PartyDetails respondingParty);

    default Optional<Response> findResponse(PartyDetails respondingParty) {
        if (respondingParty != null) {
            return ofNullable(respondingParty.getResponse());
        }
        return Optional.empty();
    }
}
