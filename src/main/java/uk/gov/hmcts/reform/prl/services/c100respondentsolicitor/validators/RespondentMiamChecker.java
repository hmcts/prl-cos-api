package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentMiamChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty, boolean isC1aApplicable) {
        Optional<Response> response = findResponse(respondingParty);

        return response.filter(value -> ofNullable(value.getMiam())
            .filter(miam -> anyNonEmpty(
                miam.getAttendedMiam(),
                miam.getReasonNotAttendingMiam(),
                miam.getWillingToAttendMiam()
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty, boolean isC1aApplicable) {
        Optional<Response> response = findResponse(respondingParty);
        boolean isFinished = false;
        respondentTaskErrorService.addEventError(
            MIAM,
            MIAM_ERROR,
            MIAM_ERROR.getError()
        );
        if (response.isPresent()) {
            Optional<Miam> miam
                = Optional.ofNullable(response.get().getMiam());
            if (miam.isPresent() && checkMiamManadatoryCompleted(miam.get())) {
                respondentTaskErrorService.removeError(MIAM_ERROR);
                isFinished = true;
            }
        }
        return isFinished;
    }

    private boolean checkMiamManadatoryCompleted(Miam miam) {
        List<Optional<?>> fields = new ArrayList<>();
        log.info("entering miam checker if loop...");
        fields.add(ofNullable(miam.getAttendedMiam()));
        Optional<YesOrNo> attendMiam = ofNullable(miam.getAttendedMiam());
        if (attendMiam.isPresent()
            && YesOrNo.No.equals(attendMiam.get())) {
            fields.add(ofNullable(miam.getWillingToAttendMiam()));
            YesOrNo willingToAttendMiam = miam.getWillingToAttendMiam();
            if (YesOrNo.No.equals(willingToAttendMiam)) {
                fields.add(ofNullable(miam.getReasonNotAttendingMiam()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }
}
