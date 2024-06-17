package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.RESPONSE_TO_ALLEGATION_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.RESPOND_ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResponseToAllegationsOfHarmChecker implements RespondentEventChecker {

    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty, boolean isC1aApplicable) {
        if (isC1aApplicable) {
            Optional<Response> response = findResponse(respondingParty);
            if (response.isPresent()) {
                return ofNullable(response.get().getResponseToAllegationsOfHarm())
                    .filter(responseToAllegationsOfHarm -> anyNonEmpty(
                        responseToAllegationsOfHarm.getResponseToAllegationsOfHarmYesOrNoResponse(),
                        responseToAllegationsOfHarm.getResponseToAllegationsOfHarmDocument()
                    )).isPresent();
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty, boolean isC1aApplicable) {
        if (isC1aApplicable) {
            Optional<Response> response = findResponse(respondingParty);
            if (response.isPresent()) {
                Optional<ResponseToAllegationsOfHarm> responseToAllegationsOfHarm = Optional.ofNullable(
                    response.get()
                        .getResponseToAllegationsOfHarm());
                if (!responseToAllegationsOfHarm.isEmpty() && checkResponseToAllegationsOfHarmMandatoryCompleted(
                    responseToAllegationsOfHarm)) {
                    respondentTaskErrorService.removeError(RESPONSE_TO_ALLEGATION_OF_HARM_ERROR);
                    return true;
                }
            }
            respondentTaskErrorService.addEventError(
                RESPOND_ALLEGATION_OF_HARM,
                RESPONSE_TO_ALLEGATION_OF_HARM_ERROR,
                RESPONSE_TO_ALLEGATION_OF_HARM_ERROR.getError()
            );
            return false;
        } else {
            return true;
        }
    }

    private boolean checkResponseToAllegationsOfHarmMandatoryCompleted(Optional<ResponseToAllegationsOfHarm> responseToAllegationsOfHarm) {

        List<Optional<?>> fields = new ArrayList<>();
        if (responseToAllegationsOfHarm.isPresent()) {
            Optional<YesOrNo> responseToAllegationsOfHarmYesOrNo = ofNullable(responseToAllegationsOfHarm
                    .get().getResponseToAllegationsOfHarmYesOrNoResponse());
            fields.add(responseToAllegationsOfHarmYesOrNo);
            if (responseToAllegationsOfHarmYesOrNo.isPresent() && YesOrNo.Yes.equals(responseToAllegationsOfHarmYesOrNo.get())) {
                fields.add(ofNullable(responseToAllegationsOfHarm.get().getResponseToAllegationsOfHarmDocument()));
            }
        }

        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }
}
