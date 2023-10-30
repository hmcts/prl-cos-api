package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.KEEP_DETAILS_PRIVATE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KeepDetailsPrivateChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean returnValue = false;
        if (response.isPresent()) {
            Optional<KeepDetailsPrivate> keepDetailsPrivateOptional
                = ofNullable(response.get().getKeepDetailsPrivate());
            if (keepDetailsPrivateOptional.isPresent()) {
                returnValue = ofNullable(keepDetailsPrivateOptional.get())
                    .filter(x -> anyNonEmpty(
                        x.getConfidentiality(),
                        x.getOtherPeopleKnowYourContactDetails(),
                        x.getConfidentialityList()
                    )).isPresent();
            }
        }

        return returnValue;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<KeepDetailsPrivate> keepDetailsPrivate = Optional.ofNullable(response.get()
                                                                                               .getKeepDetailsPrivate());
            if (!keepDetailsPrivate.isEmpty() && checkKeepDetailsPrivateMandatoryCompleted(keepDetailsPrivate)) {
                respondentTaskErrorService.removeError(KEEP_DETAILS_PRIVATE_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            KEEP_DETAILS_PRIVATE,
            KEEP_DETAILS_PRIVATE_ERROR,
            KEEP_DETAILS_PRIVATE_ERROR.getError()
        );
        return false;
    }

    private boolean checkKeepDetailsPrivateMandatoryCompleted(Optional<KeepDetailsPrivate> keepDetailsPrivate) {

        List<Optional<?>> fields = new ArrayList<>();
        if (keepDetailsPrivate.isPresent()) {
            fields.add(ofNullable(keepDetailsPrivate.get().getOtherPeopleKnowYourContactDetails()));
            Optional<YesOrNo> confidentiality = ofNullable(keepDetailsPrivate.get().getConfidentiality());
            fields.add(confidentiality);
            if (confidentiality.isPresent() && confidentiality.equals(Optional.of(YesOrNo.Yes))) {
                fields.add(ofNullable(keepDetailsPrivate.get().getConfidentialityList()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
