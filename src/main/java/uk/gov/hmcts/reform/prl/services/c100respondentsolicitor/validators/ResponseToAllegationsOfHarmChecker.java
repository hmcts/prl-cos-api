package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseToAllegationsOfHarmChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        return false;
    }
}
