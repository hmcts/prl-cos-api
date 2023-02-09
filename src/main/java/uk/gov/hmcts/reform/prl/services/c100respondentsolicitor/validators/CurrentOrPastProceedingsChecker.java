package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class CurrentOrPastProceedingsChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return activeRespondent.filter(partyDetailsElement -> anyNonEmpty(partyDetailsElement
                                                                              .getValue()
                                                                              .getResponse()
                                                                              .getCurrentOrPastProceedingsForChildren()
        )).isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        List<Optional<?>> fields = new ArrayList<>();
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        if (activeRespondent.isPresent()) {
            fields.add(ofNullable(activeRespondent.get()
                                      .getValue()
                                      .getResponse()
                                      .getCurrentOrPastProceedingsForChildren()));

            YesNoDontKnow currentOrPastProceedingsForChildren = activeRespondent.get()
                .getValue()
                .getResponse()
                .getCurrentOrPastProceedingsForChildren();

            if (currentOrPastProceedingsForChildren.equals(YesNoDontKnow.yes)) {
                fields.add(ofNullable(activeRespondent.get().getValue()
                                          .getResponse().getRespondentExistingProceedings()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

}
