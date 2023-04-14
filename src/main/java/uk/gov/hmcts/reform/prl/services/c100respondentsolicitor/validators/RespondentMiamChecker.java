package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
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
public class RespondentMiamChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response.filter(value -> ofNullable(value.getSolicitorMiam())
            .filter(miam -> anyNonEmpty(
                miam.getRespSolHaveYouAttendedMiam().getAttendedMiam(),
                miam.getRespSolHaveYouAttendedMiam().getReasonNotAttendingMiam(),
                miam.getRespSolHaveYouAttendedMiam().getWillingToAttendMiam(),
                miam.getRespSolWillingnessToAttendMiam().getAttendedMiam(),
                miam.getRespSolWillingnessToAttendMiam().getReasonNotAttendingMiam(),
                miam.getRespSolWillingnessToAttendMiam().getWillingToAttendMiam()
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<SolicitorMiam> miam
                = Optional.ofNullable(response.get().getSolicitorMiam());
            if (miam.isPresent() && checkMiamManadatoryCompleted(miam)) {
                respondentTaskErrorService.removeError(MIAM_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            MIAM,
            MIAM_ERROR,
            MIAM_ERROR.getError()
        );
        return false;
    }

    private boolean checkMiamManadatoryCompleted(Optional<SolicitorMiam> miam) {
        List<Optional<?>> fields = new ArrayList<>();
        log.info("entering miam checker if loop...");
        if (miam.isPresent()) {
            fields.add(ofNullable(miam.get().getRespSolHaveYouAttendedMiam().getAttendedMiam()));
            YesOrNo attendMiam = miam.get().getRespSolHaveYouAttendedMiam().getAttendedMiam();
            if (YesOrNo.No.equals(attendMiam)) {
                fields.add(ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getWillingToAttendMiam()));
                YesOrNo willingToAttendMiam = miam.get().getRespSolWillingnessToAttendMiam().getWillingToAttendMiam();
                if (YesOrNo.No.equals(willingToAttendMiam)) {
                    fields.add(ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getReasonNotAttendingMiam()));
                }
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
