package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.OTHER_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CurrentOrPastProceedingsChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty, boolean isC1aApplicable) {
        Optional<Response> response = findResponse(respondingParty);
        boolean isStarted = false;
        if (response.isPresent()) {
            isStarted = ofNullable(response.get().getCurrentOrPastProceedingsForChildren())
                .filter(proceedings -> anyNonEmpty(
                    proceedings.getDisplayedValue()
                )).isPresent();
        }
        if (isStarted) {
            respondentTaskErrorService.addEventError(
                OTHER_PROCEEDINGS,
                OTHER_PROCEEDINGS_ERROR,
                OTHER_PROCEEDINGS_ERROR.getError()
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty, boolean isC1aApplicable) {
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
            respondentTaskErrorService.removeError(OTHER_PROCEEDINGS_ERROR);
            return true;
        } else {
            return false;
        }
    }
}
