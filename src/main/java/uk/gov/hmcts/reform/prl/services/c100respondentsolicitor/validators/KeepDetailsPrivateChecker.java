package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class KeepDetailsPrivateChecker implements RespondentEventChecker {

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
                               .getKeepDetailsPrivate()
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

        Optional<SolicitorKeepDetailsPrivate> keepDetailsPrivate = Optional.ofNullable(activeRespondent.get()
                                                            .getValue()
                                                            .getResponse()
                                                            .getSolicitorKeepDetailsPriate());
        if (!keepDetailsPrivate.isEmpty()) {
            if (checkKeepDetailsPrivateMandatoryCompleted(keepDetailsPrivate)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkKeepDetailsPrivateMandatoryCompleted(Optional<SolicitorKeepDetailsPrivate> keepDetailsPrivate) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(keepDetailsPrivate.get().getRespKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails()));
        Optional<YesOrNo> confidentiality = ofNullable(keepDetailsPrivate.get()
                                                           .getRespKeepDetailsPrivateConfidentiality().getConfidentiality());
        fields.add(confidentiality);
        if (confidentiality.isPresent() && confidentiality.equals(Optional.of(YesOrNo.Yes))) {
            fields.add(ofNullable(keepDetailsPrivate.get()
                                      .getRespKeepDetailsPrivateConfidentiality().getConfidentialityList()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
