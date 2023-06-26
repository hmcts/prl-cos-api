package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

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
public class RespondentMiamChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response.filter(value -> ofNullable(value.getMiam())
            .filter(miam -> anyNonEmpty(
                miam.getAttendedMiam(),
                miam.getReasonNotAttendingMiam(),
                miam.getWillingToAttendMiam(),
                miam.getAttendedMiam(),
                miam.getReasonNotAttendingMiam(),
                miam.getWillingToAttendMiam()
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        boolean returnValue = false;
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<Miam> miam
                = Optional.ofNullable(response.get().getMiam());
            if (miam.isPresent() && checkMiamManadatoryCompleted(miam)) {
                respondentTaskErrorService.removeError(MIAM_ERROR);
                returnValue = true;
            }
        }

        if (returnValue) {
            respondentTaskErrorService.addEventError(
                MIAM,
                MIAM_ERROR,
                MIAM_ERROR.getError()
            );
        }
        return returnValue;
    }

    private boolean checkMiamManadatoryCompleted(Optional<Miam> miam) {
        List<Optional<?>> fields = new ArrayList<>();
        log.info("entering miam checker if loop...");
        if (miam.isPresent()) {
            fields.add(ofNullable(miam.get().getAttendedMiam()));
            Optional<YesOrNo> attendMiam = ofNullable(miam.get().getAttendedMiam());
            if (attendMiam.isPresent()
                && YesOrNo.No.equals(attendMiam.get())) {
                fields.add(ofNullable(miam.get().getWillingToAttendMiam()));
                YesOrNo willingToAttendMiam = miam.get().getWillingToAttendMiam();
                if (YesOrNo.No.equals(willingToAttendMiam)) {
                    fields.add(ofNullable(miam.get().getReasonNotAttendingMiam()));
                }
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
