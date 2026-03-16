package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.LocalAuthority;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LocalAuthorityGeneratorTest {

    private final LocalAuthorityGenerator generator = new LocalAuthorityGenerator();

    @Test
    public void testGenerate() {

        LocalAuthority localAuthority = LocalAuthority.builder()
            .localAuthoritySolicitorOrganisationName("Solicitor Organisation")
            .isLocalAuthorityInvolvedInCase(YesOrNo.Yes)
            .build();

        CaseSummary caseSummary = generator.generate(CaseData.builder().localAuthority(localAuthority).build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().localAuthority(localAuthority).build());

    }

    @Test
    public void testIfOrderAppliedForNotSelected() {
        CaseSummary caseSummary = generator.generate(CaseData.builder().localAuthority(null).build());


        LocalAuthority localAuthority = LocalAuthority.builder()
            .isLocalAuthorityInvolvedInCase(YesOrNo.No)
            .localAuthoritySolicitorOrganisationName(null).build();
        assertThat(caseSummary).isEqualTo(CaseSummary.builder().localAuthority(localAuthority).build());
    }
}
