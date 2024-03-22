package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedingEmptyTable;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

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
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
            Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());
            return proceedingsCheck.isPresent() && (proceedingCheck.isEmpty() || proceedingCheck.get()
                .equals(YesNoDontKnow.yes));
        }

        Optional<FL401OtherProceedingDetails> proceedingObject = ofNullable(caseData.getFl401OtherProceedingDetails());

        Optional<YesNoDontKnow> proceedingCheck = Optional.empty();
        Optional<List<Element<FL401Proceedings>>> proceedingsCheck = Optional.empty();
        if (proceedingObject.isPresent()) {
            proceedingCheck = ofNullable(proceedingObject.get().getHasPrevOrOngoingOtherProceeding());
            proceedingsCheck = ofNullable(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());
        }
        return proceedingsCheck.isPresent() && (proceedingCheck.isEmpty() || proceedingCheck.get()
            .equals(YesNoDontKnow.yes));
    }

    public List<Element<OtherProceedings>> getOtherProceedingsDetails(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100OtherProceedingsDetails(caseData);
        }

        return getFl401OtherProceedingsDetails(caseData);
    }

    public List<Element<OtherProceedings>> getC100OtherProceedingsDetails(CaseData caseData) {
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());
        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent() && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            OtherProceedings op = OtherProceedings.builder().build();
            Element<OtherProceedings> other = Element.<OtherProceedings>builder().value(op).build();
            return Collections.singletonList(other);
        }
        List<ProceedingDetails> proceedings = caseData.getExistingProceedings().stream()
            .map(Element::getValue).toList();
        List<Element<OtherProceedings>> otherProceedingsDetailsList = new ArrayList<>();

        for (ProceedingDetails p : proceedings) {
            String ordersMade = null != p.getTypeOfOrder() ? p.getTypeOfOrder().stream().map(TypeOfOrderEnum::getDisplayedValue)
                .collect(Collectors.joining(", ")) : null;

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

    public List<Element<OtherProceedings>> getFl401OtherProceedingsDetails(CaseData caseData) {
        Optional<FL401OtherProceedingDetails> proceedingObject = ofNullable(caseData.getFl401OtherProceedingDetails());

        Optional<YesNoDontKnow> proceedingCheck = Optional.empty();
        Optional<List<Element<FL401Proceedings>>> proceedingsCheck = Optional.empty();
        if (proceedingObject.isPresent()) {
            proceedingCheck = ofNullable(proceedingObject.get().getHasPrevOrOngoingOtherProceeding());
            proceedingsCheck = ofNullable(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());
        }

        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent() && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            OtherProceedings op = OtherProceedings.builder().build();
            Element<OtherProceedings> other = Element.<OtherProceedings>builder().value(op).build();
            return Collections.singletonList(other);
        }

        List<FL401Proceedings> proceedings = caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().stream()
            .map(Element::getValue).toList();
        List<Element<OtherProceedings>> otherProceedingsDetailsList = new ArrayList<>();

        for (FL401Proceedings p : proceedings) {
            String typeOfOrder = null != p.getTypeOfCase() ? p.getTypeOfCase() : null;
            OtherProceedings otherProceedingsDetails = OtherProceedings.builder()
                .caseNumber(p.getCaseNumber())
                .typeOfOrder(typeOfOrder)
                .nameOfCourt(p.getNameOfCourt())
                .build();

            Element<OtherProceedings> details = Element.<OtherProceedings>builder()
                .value(otherProceedingsDetails).build();
            otherProceedingsDetailsList.add(details);
        }
        return otherProceedingsDetailsList;
    }


}
