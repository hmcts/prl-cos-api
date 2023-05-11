package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.CURRENT_OR_PREVIOUS_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class CurrentOrPastProceedingsChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            return ofNullable(response.get().getCurrentOrPastProceedingsForChildren())
                .filter(proceedings -> anyNonEmpty(
                    proceedings.getDisplayedValue()
                )).isPresent();
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        List<Optional<?>> fields = new ArrayList<>();

        if (response.isPresent()) {
            fields.add(ofNullable(response.get().getCurrentOrPastProceedingsForChildren()));

            Optional<YesNoDontKnow> currentOrPastProceedingsForChildren = ofNullable(response.get().getCurrentOrPastProceedingsForChildren());

            if (currentOrPastProceedingsForChildren.isPresent()
                && YesNoDontKnow.yes.equals(currentOrPastProceedingsForChildren.get())) {
                fields.add(ofNullable(response.get().getRespondentExistingProceedings()));
            }
        }
        if (fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .noneMatch(field -> field.equals(""))) {
            respondentTaskErrorService.removeError(CURRENT_OR_PREVIOUS_PROCEEDINGS_ERROR);
            return true;
        } else {
            respondentTaskErrorService.addEventError(
                CURRENT_OR_PREVIOUS_PROCEEDINGS,
                CURRENT_OR_PREVIOUS_PROCEEDINGS_ERROR,
                CURRENT_OR_PREVIOUS_PROCEEDINGS_ERROR.getError()
            );
            return false;
        }
    }
}
