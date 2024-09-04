package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ATTENDING_THE_COURT_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AttendToCourtChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty, boolean isC1aApplicable) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            return ofNullable(response.get().getAttendToCourt())
                .filter(attendToCourt -> anyNonEmpty(
                    attendToCourt.getRespondentWelshNeeds(),
                    attendToCourt.getRespondentWelshNeedsList(),
                    attendToCourt.getIsRespondentNeededInterpreter(),
                    attendToCourt.getRespondentInterpreterNeeds(),
                    attendToCourt.getHaveAnyDisability(),
                    attendToCourt.getDisabilityNeeds(),
                    attendToCourt.getRespondentSpecialArrangements(),
                    attendToCourt.getRespondentSpecialArrangementDetails(),
                    attendToCourt.getRespondentIntermediaryNeeds(),
                    attendToCourt.getRespondentIntermediaryNeedDetails()
                )).isPresent();
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty, boolean isC1aApplicable) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<AttendToCourt> attendToCourt = Optional.ofNullable(response.get()
                                                                            .getAttendToCourt());
            if (!attendToCourt.isEmpty() && checkAttendToCourtMandatoryCompleted(attendToCourt)) {
                respondentTaskErrorService.removeError(ATTENDING_THE_COURT_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            ATTENDING_THE_COURT,
            ATTENDING_THE_COURT_ERROR,
            ATTENDING_THE_COURT_ERROR.getError()
        );
        return false;
    }

    private boolean checkAttendToCourtMandatoryCompleted(Optional<AttendToCourt> attendToCourt) {

        List<Optional<?>> fields = new ArrayList<>();
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> respondentWelshNeeds = ofNullable(attendToCourt.get().getRespondentWelshNeeds());
            fields.add(respondentWelshNeeds);
            if (respondentWelshNeeds.isPresent() && YesOrNo.Yes.equals(respondentWelshNeeds.get())) {
                fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeedsList()));
            }
            isRespondentNeededInterpreter(attendToCourt, fields);
            haveAnyDisability(attendToCourt, fields);
            respondentSpecialArrangements(attendToCourt, fields);
            respondentIntermediaryNeeds(attendToCourt, fields);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    private static void respondentIntermediaryNeeds(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> respondentIntermediaryNeeds = ofNullable(attendToCourt.get().getRespondentIntermediaryNeeds());
            fields.add(respondentIntermediaryNeeds);
            if (respondentIntermediaryNeeds.isPresent() && YesOrNo.Yes.equals(respondentIntermediaryNeeds.get())) {
                fields.add(ofNullable(attendToCourt.get().getRespondentIntermediaryNeedDetails()));
            }
        }
    }

    private static void respondentSpecialArrangements(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> respondentSpecialArrangements = ofNullable(attendToCourt.get().getRespondentSpecialArrangements());
            fields.add(respondentSpecialArrangements);
            if (respondentSpecialArrangements.isPresent() && YesOrNo.Yes.equals(respondentSpecialArrangements.get())) {
                fields.add(ofNullable(attendToCourt.get().getRespondentSpecialArrangementDetails()));
            }
        }
    }

    private static void haveAnyDisability(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> haveAnyDisability = ofNullable(attendToCourt.get().getHaveAnyDisability());
            fields.add(haveAnyDisability);
            if (haveAnyDisability.isPresent() && YesOrNo.Yes.equals(haveAnyDisability.get())) {
                fields.add(ofNullable(attendToCourt.get().getDisabilityNeeds()));
            }
        }
    }

    private static void isRespondentNeededInterpreter(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> isRespondentNeededInterpreter = ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter());
            fields.add(isRespondentNeededInterpreter);
            if (isRespondentNeededInterpreter.isPresent() && YesOrNo.Yes.equals(isRespondentNeededInterpreter.get())) {
                List<RespondentInterpreterNeeds> respondentInterpreterNeeds = attendToCourt.get()
                    .getRespondentInterpreterNeeds()
                    .stream()
                    .map(Element::getValue)
                    .toList();
                for (RespondentInterpreterNeeds interpreterNeeds : respondentInterpreterNeeds) {
                    fields.add(ofNullable(interpreterNeeds.getParty()));
                    fields.add(ofNullable(interpreterNeeds.getRelationName()));
                    fields.add(ofNullable(interpreterNeeds.getRequiredLanguage()));

                }
            }
        }
    }
}
