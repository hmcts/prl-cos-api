package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return anyNonEmpty(activeRespondent
                               .get()
                               .getValue()
                               .getResponse()
                               .getAttendToCourt()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        boolean mandatoryInfo = false;

        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        Optional<AttendToCourt> attendToCourt = Optional.ofNullable(activeRespondent.get()
                                                                                                      .getValue()
                                                                                                      .getResponse()
                                                                                                      .getAttendToCourt());
        if (!attendToCourt.isEmpty()) {
            if (checkAttendToCourtManadatoryCompleted(attendToCourt)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkAttendToCourtManadatoryCompleted(Optional<AttendToCourt> attendToCourt) {

        List<Optional<?>> fields = new ArrayList<>();
        Optional<YesOrNo> respondentWelshNeeds = ofNullable(attendToCourt.get().getRespondentWelshNeeds());
        fields.add(respondentWelshNeeds);
        if (respondentWelshNeeds.isPresent() && respondentWelshNeeds.equals(YesOrNo.Yes)) {
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeedsList()));
        }
        Optional<YesOrNo> isRespondentNeededInterpreter = ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter());
        fields.add(isRespondentNeededInterpreter);
        if (isRespondentNeededInterpreter.isPresent() && isRespondentNeededInterpreter.equals(YesOrNo.Yes)) {
            List<RespondentInterpreterNeeds> respondentInterpreterNeeds = attendToCourt.get()
                .getRespondentInterpreterNeeds()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (RespondentInterpreterNeeds interpreterNeeds  : respondentInterpreterNeeds) {
                fields.add(ofNullable(interpreterNeeds.getParty()));
                fields.add(ofNullable(interpreterNeeds.getRelationName()));
                fields.add(ofNullable(interpreterNeeds.getRequiredLanguage()));

            }
        }
        Optional<YesOrNo> haveAnyDisability = ofNullable(attendToCourt.get().getHaveAnyDisability());
        fields.add(haveAnyDisability);
        if (haveAnyDisability.isPresent() && haveAnyDisability.equals(YesOrNo.Yes)) {
            fields.add(ofNullable(attendToCourt.get().getDisabilityNeeds()));
        }
        Optional<YesOrNo> respondentSpecialArrangements = ofNullable(attendToCourt.get().getRespondentSpecialArrangements());
        fields.add(respondentSpecialArrangements);
        if (respondentSpecialArrangements.isPresent() && respondentSpecialArrangements.equals(YesOrNo.Yes)) {
            fields.add(ofNullable(attendToCourt.get().getRespondentSpecialArrangementDetails()));
        }
        Optional<YesOrNo> respondentIntermediaryNeeds = ofNullable(attendToCourt.get().getRespondentIntermediaryNeeds());
        fields.add(respondentIntermediaryNeeds);
        if (respondentIntermediaryNeeds.isPresent() && respondentIntermediaryNeeds.equals(YesOrNo.Yes)) {
            fields.add(ofNullable(attendToCourt.get().getRespondentIntermediaryNeedDetails()));
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }
}
