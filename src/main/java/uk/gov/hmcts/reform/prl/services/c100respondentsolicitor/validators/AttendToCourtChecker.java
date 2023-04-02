package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class AttendToCourtChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData, String respondent) {
        Optional<Response> response = findResponse(caseData, respondent);

        return response
            .filter(res -> anyNonEmpty(res.getAttendToCourt()
            )).isPresent();
    }

    @Override
    public boolean isFinished(CaseData caseData, String respondent) {
        boolean mandatoryInfo = false;
        Optional<Response> response = findResponse(caseData, respondent);

        if (response.isPresent()) {
            Optional<AttendToCourt> attendToCourt = Optional.ofNullable(response.get()
                                                                            .getAttendToCourt());
            if (!attendToCourt.isEmpty() && checkAttendToCourtMandatoryCompleted(attendToCourt)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkAttendToCourtMandatoryCompleted(Optional<AttendToCourt> attendToCourt) {

        List<Optional<?>> fields = new ArrayList<>();
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> respondentWelshNeeds = ofNullable(attendToCourt.get().getRespondentWelshNeeds());
            fields.add(respondentWelshNeeds);
            if (respondentWelshNeeds.isPresent() && respondentWelshNeeds.equals(Optional.of((YesOrNo.Yes)))) {
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
            if (respondentIntermediaryNeeds.isPresent() && respondentIntermediaryNeeds.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(attendToCourt.get().getRespondentIntermediaryNeedDetails()));
            }
        }
    }

    private static void respondentSpecialArrangements(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> respondentSpecialArrangements = ofNullable(attendToCourt.get().getRespondentSpecialArrangements());
            fields.add(respondentSpecialArrangements);
            if (respondentSpecialArrangements.isPresent() && respondentSpecialArrangements.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(attendToCourt.get().getRespondentSpecialArrangementDetails()));
            }
        }
    }

    private static void haveAnyDisability(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> haveAnyDisability = ofNullable(attendToCourt.get().getHaveAnyDisability());
            fields.add(haveAnyDisability);
            if (haveAnyDisability.isPresent() && haveAnyDisability.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(attendToCourt.get().getDisabilityNeeds()));
            }
        }
    }

    private static void isRespondentNeededInterpreter(Optional<AttendToCourt> attendToCourt, List<Optional<?>> fields) {
        if (attendToCourt.isPresent()) {
            Optional<YesOrNo> isRespondentNeededInterpreter = ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter());
            fields.add(isRespondentNeededInterpreter);
            if (isRespondentNeededInterpreter.isPresent() && isRespondentNeededInterpreter.equals(Optional.of((YesOrNo.Yes)))) {
                List<RespondentInterpreterNeeds> respondentInterpreterNeeds = attendToCourt.get()
                    .getRespondentInterpreterNeeds()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                for (RespondentInterpreterNeeds interpreterNeeds : respondentInterpreterNeeds) {
                    fields.add(ofNullable(interpreterNeeds.getParty()));
                    fields.add(ofNullable(interpreterNeeds.getRelationName()));
                    fields.add(ofNullable(interpreterNeeds.getRequiredLanguage()));

                }
            }
        }
    }
}
