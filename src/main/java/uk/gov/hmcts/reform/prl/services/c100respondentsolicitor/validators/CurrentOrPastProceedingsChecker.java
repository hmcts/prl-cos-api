package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
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
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return anyNonEmpty(activeRespondent
                               .get()
                               .getValue()
                               .getResponse()
                               .getCurrentOrPastProceedingsForChildren()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        List<Optional<?>> fields = new ArrayList<>();
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        Optional<YesNoDontKnow> currentOrPastProceedingsForChildren = Optional.ofNullable(activeRespondent.get()
                                                      .getValue()
                                                      .getResponse()
                                                      .getCurrentOrPastProceedingsForChildren());
        fields.add(ofNullable(currentOrPastProceedingsForChildren));
        if (currentOrPastProceedingsForChildren.isPresent() && currentOrPastProceedingsForChildren.equals(YesOrNo.Yes)) {

            Optional<List<Element<ProceedingDetails>>> existingProceedings = ofNullable(activeRespondent.get().getValue()
                                                                                         .getResponse().getRespondentExistingProceedings());
            fields.add(ofNullable(existingProceedings));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

}
