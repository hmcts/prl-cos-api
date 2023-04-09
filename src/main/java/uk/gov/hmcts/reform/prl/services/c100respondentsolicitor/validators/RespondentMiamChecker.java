package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
public class RespondentMiamChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response
            .filter(res -> anyNonEmpty(res.getSolicitorMiam()
            )).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean mandatoryInfo = false;

        if (response.isPresent()) {
            Optional<SolicitorMiam> miam
                = Optional.ofNullable(response.get().getSolicitorMiam());
            if (!miam.isEmpty()) {
                log.info("before checking miam mandatory info if loop...");
                if (checkMiamManadatoryCompleted(miam)) {
                    log.info("inside checking miam mandatory info if loop...");
                    mandatoryInfo = true;
                }
            }
        }
        return mandatoryInfo;
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
