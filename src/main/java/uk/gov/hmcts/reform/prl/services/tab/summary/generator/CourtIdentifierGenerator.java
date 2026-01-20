package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CourtIdentifier;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HIGH_COURT;

@Slf4j
@Component
public class CourtIdentifierGenerator  implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        log.info("CourtIdentifier Generator invoked");
        return CaseSummary.builder()
            .courtIdentifier(courtIdentifierFromCaseData(caseData))
            .build();
    }

    public CourtIdentifier courtIdentifierFromCaseData(CaseData caseData) {
        YesOrNo isHighCourtCase = caseData.getIsHighCourtCase();
        String courtIdentifier = YesOrNo.Yes.equals(isHighCourtCase) ? HIGH_COURT : null;
        log.info("courtIdentifier {}", courtIdentifier);
        return CourtIdentifier.builder()
            .courtIdentifier(courtIdentifier)
            .build();
    }

}
