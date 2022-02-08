package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedingEmptyTable;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
public class OtherProceedingsGenerator implements  FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        List<Element<OtherProceedings>> otherProceedingsDetails = getOtherProceedingsDetails(caseData);

        return CaseSummary.builder().otherProceedingsForSummaryTab(otherProceedingsDetails)
            .otherProceedingEmptyTable(OtherProceedingEmptyTable.builder()
                                           .otherProceedingEmptyField(hasOtherProceedings(caseData) ? "" : " ")
                                           .build()).build();
    }

    private boolean hasOtherProceedings(CaseData caseData) {
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());
        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent()
            && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            return false;
        }
        return true;
    }

    public List<Element<OtherProceedings>> getOtherProceedingsDetails(CaseData caseData) {
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());
        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent() && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            OtherProceedings op = OtherProceedings.builder().build();
            Element<OtherProceedings> other = Element.<OtherProceedings>builder().value(op).build();
            return Collections.singletonList(other);
        }
        List<ProceedingDetails> proceedings = caseData.getExistingProceedings().stream()
            .map(Element::getValue).collect(Collectors.toList());
        List<Element<OtherProceedings>> otherProceedingsDetailsList = new ArrayList<>();

        for (ProceedingDetails p : proceedings) {
            String ordersMade = p.getTypeOfOrder().stream().map(TypeOfOrderEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));

            OtherProceedings otherProceedingsDetails = OtherProceedings.builder()
                .caseNumber(p.getCaseNumber())
                .typeOfOrder(ordersMade)
                .nameOfCourt(p.getNameOfCourt())
                .build();

            Element<OtherProceedings> details = Element.<OtherProceedings>builder()
                .value(otherProceedingsDetails).build();
            otherProceedingsDetailsList.add(details);
        }
        return otherProceedingsDetailsList;
    }
}
