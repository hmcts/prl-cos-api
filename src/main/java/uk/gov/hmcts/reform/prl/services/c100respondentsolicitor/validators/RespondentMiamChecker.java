package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RespondentMiamChecker implements RespondentEventChecker {
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
                               .getMiam()
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

        Optional<SolicitorMiam> miam = Optional.ofNullable(activeRespondent.get()
                                                      .getValue()
                                                      .getResponse()
                                                      .getSolicitorMiam());
        if (!miam.isEmpty()) {
            if (checkMiamManadatoryCompleted(miam)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkMiamManadatoryCompleted(Optional<SolicitorMiam> miam) {
        List<Optional<?>> fields = new ArrayList<>();
        Optional<YesOrNo> attendMiam = ofNullable(miam.get().getRespSolHaveYouAttendedMiam().getAttendedMiam());
        fields.add(attendMiam);
        if (attendMiam.isPresent() && attendMiam.equals(YesOrNo.No)) {
            Optional<YesOrNo> willingToAttendMiam = ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getWillingToAttendMiam());
            fields.add(willingToAttendMiam);
            if (willingToAttendMiam.isPresent() && willingToAttendMiam.equals(YesOrNo.No)) {
                fields.add(ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getReasonNotAttendingMiam()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
