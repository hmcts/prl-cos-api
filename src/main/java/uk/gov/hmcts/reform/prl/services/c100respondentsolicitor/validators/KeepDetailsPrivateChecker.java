package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

        Optional<KeepDetailsPrivate> keepDetailsPrivate = activeRespondent.map(
            partyDetailsElement -> Optional.ofNullable(partyDetailsElement
                                                            .getValue()
                                                           .getResponse()
                                                           .getKeepDetailsPrivate()))
            .orElse(null);
        if (Objects.requireNonNull(keepDetailsPrivate).isPresent()) {
            if (checkKeepDetailsPrivateMandatoryCompleted(keepDetailsPrivate)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkKeepDetailsPrivateMandatoryCompleted(Optional<KeepDetailsPrivate> keepDetailsPrivate) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(keepDetailsPrivate.get().getOtherPeopleKnowYourContactDetails()));
        fields.add(ofNullable(keepDetailsPrivate.get().getConfidentiality()));
        if (keepDetailsPrivate.get().getConfidentiality().equals(YesOrNo.Yes)) {
            fields.add(ofNullable(keepDetailsPrivate.get().getConfidentialityList()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
