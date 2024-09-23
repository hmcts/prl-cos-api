package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Component
@Slf4j
public class RefugeCaseGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        YesOrNo isRefugeCase = YesOrNo.No;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            isRefugeCase = findIfC100PartyLivesInRefuge(caseData.getApplicants());

            if (YesOrNo.No.equals(isRefugeCase)) {
                isRefugeCase = findIfC100PartyLivesInRefuge(caseData.getRespondents());
            }
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            isRefugeCase = caseData.getApplicantsFL401().getLiveInRefuge();
            if (YesOrNo.No.equals(isRefugeCase)) {
                isRefugeCase = caseData.getRespondentsFL401().getLiveInRefuge();
            }
        }
        return CaseSummary.builder().isRefugeCase(isRefugeCase).build();
    }

    private static YesOrNo findIfC100PartyLivesInRefuge(List<Element<PartyDetails>> caseData) {
        Optional<Element<PartyDetails>> refugeParty
            = caseData
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getLiveInRefuge()))
            .findFirst();

        return refugeParty.isPresent() ? YesOrNo.Yes : YesOrNo.No;
    }
}
