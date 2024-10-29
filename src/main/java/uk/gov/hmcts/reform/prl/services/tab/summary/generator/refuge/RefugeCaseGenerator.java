package uk.gov.hmcts.reform.prl.services.tab.summary.generator.refuge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge.RefugeCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Component
@Slf4j
public class RefugeCaseGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        log.info("Inside refuge case generator");
        YesOrNo isRefugeCase = YesOrNo.No;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            isRefugeCase = findIfC100PartyLivesInRefuge(caseData.getApplicants());

            if (YesOrNo.No.equals(isRefugeCase)) {
                isRefugeCase = findIfC100PartyLivesInRefuge(caseData.getRespondents());
            }
            if (YesOrNo.No.equals(isRefugeCase)) {
                isRefugeCase = findIfC100PartyLivesInRefuge(caseData.getOtherPartyInTheCaseRevised());
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (caseData.getApplicantsFL401() != null) {
                isRefugeCase = caseData.getApplicantsFL401().getLiveInRefuge();
            }
            if (YesOrNo.No.equals(isRefugeCase) && (caseData.getRespondentsFL401() != null)) {
                isRefugeCase = caseData.getRespondentsFL401().getLiveInRefuge();
            }
        }
        log.info("Is refuge case {}", isRefugeCase);
        return CaseSummary.builder().refugeCase(RefugeCase.builder().isRefugeCase(isRefugeCase).build()).build();
    }

    private static YesOrNo findIfC100PartyLivesInRefuge(List<Element<PartyDetails>> partyDetailsList) {
        Optional<Element<PartyDetails>> refugeParty = Optional.empty();
        if (partyDetailsList != null) {
            refugeParty
                = partyDetailsList
                .stream()
                .filter(x -> YesOrNo.Yes.equals(x.getValue().getLiveInRefuge()))
                .findFirst();
        }
        return refugeParty.isPresent() ? YesOrNo.Yes : YesOrNo.No;
    }
}
