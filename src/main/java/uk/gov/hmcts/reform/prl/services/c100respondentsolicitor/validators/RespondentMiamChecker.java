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
        log.info("miam entering: isFinished: {}",respondingParty);
        Optional<Response> response = findResponse(respondingParty);
        boolean isFinished = false;
        if (response.isPresent()) {
            Optional<Miam> miam
                = Optional.ofNullable(response.get().getMiam());
            log.info(
                "miam isFinished: before check: {}",
                miam.stream().peek(event -> System.out.println("Attended Miam" + event.getAttendedMiam()))
            );
            log.info(
                "miam isFinished: before check: {}",
                miam.stream().peek(event -> System.out.println("Reason Not Attending Miam" + event.getReasonNotAttendingMiam()))
            );
            log.info(
                "miam isFinished: before check: {}",
                miam.stream().peek(event -> System.out.println("Willing To Attend Miam" + event.getWillingToAttendMiam()))
            );
            if (miam.isPresent() && checkMiamManadatoryCompleted(miam)) {
                log.info(
                    "miam isFinished: after check: {}",
                    miam.stream().peek(event -> System.out.println("Attended Miam" + event.getAttendedMiam()))
                );
                log.info(
                    "miam isFinished: after check: {}",
                    miam.stream().peek(event -> System.out.println("Reason Not Attending Miam" + event.getReasonNotAttendingMiam()))
                );
                log.info(
                    "miam isFinished: after check: {}",
                    miam.stream().peek(event -> System.out.println("Willing To Attend Miam" + event.getWillingToAttendMiam()))
                );
                respondentTaskErrorService.removeError(MIAM_ERROR);
                isFinished = true;
            }
        }
        respondentTaskErrorService.addEventError(
            MIAM,
            MIAM_ERROR,
            MIAM_ERROR.getError()
        );
        return isFinished;
    }

    private boolean checkMiamManadatoryCompleted(Optional<Miam> miam) {
        List<Optional<?>> fields = new ArrayList<>();
        log.info("entering miam checker if loop...");
        if (miam.isPresent()) {
            log.info("miam checkMiamManadatoryCompleted: AttendedMiam: {}", miam.get().getAttendedMiam());
            log.info("miam checkMiamManadatoryCompleted: WillingToAttendMiam: {}", miam.get().getWillingToAttendMiam());
            log.info(
                "miam checkMiamManadatoryCompleted: ReasonNotAttendingMiam: {}",
                miam.get().getReasonNotAttendingMiam()
            );
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
        log.info(
            "miam checkMiamManadatoryCompleted: final: {}",
            miam.stream().peek(event -> System.out.println("Attended Miam" + event.getAttendedMiam()))
        );
        log.info(
            "miam checkMiamManadatoryCompleted: final: {}",
            miam.stream().peek(event -> System.out.println("Reason Not Attending Miam" + event.getReasonNotAttendingMiam()))
        );
        log.info(
            "miam checkMiamManadatoryCompleted: final: {}",
            miam.stream().peek(event -> System.out.println("Willing To Attend Miam" + event.getWillingToAttendMiam()))
        );
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
