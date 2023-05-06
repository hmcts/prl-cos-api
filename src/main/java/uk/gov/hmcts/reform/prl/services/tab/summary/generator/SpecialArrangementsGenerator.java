package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.SpecialArrangements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Component
public class SpecialArrangementsGenerator implements  FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().specialArrangement(SpecialArrangements.builder()
                                                             .areAnySpecialArrangements(
                                                                 isSpecialArrangementAvailable(caseData))
                                                             .build()).build();
    }

    private String isSpecialArrangementAvailable(CaseData caseData) {
        return YesOrNo.Yes.equals(caseData.getAttendHearing().getIsSpecialArrangementsRequired())
            ? YesOrNo.Yes.getDisplayedValue() : YesOrNo.No.getDisplayedValue();
    }
}
