package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.LocalAuthority;

@Component
public class LocalAuthorityGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        LocalAuthority localAuthority = null != caseData.getLocalAuthority()
            ? caseData.getLocalAuthority()
            : LocalAuthority.builder().isLocalAuthorityInvolvedInCase(YesOrNo.No)
            .localAuthoritySolicitorOrganisationName(null)
            .build();
        return CaseSummary.builder().localAuthority(localAuthority).build();
    }
}
