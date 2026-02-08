package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CourtIdentifier;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.CourtIdentifierGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HIGH_COURT;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.ID;

public class CourtIdentifierGeneratorTest {

    private final CourtIdentifierGenerator courtIdentifierGenerator = new CourtIdentifierGenerator();

    @Test
    public void testGenerate() {
        //given
        CaseData caseData = CaseData.builder().id(ID).isHighCourtCase(YesOrNo.Yes).build();

        // when
        CaseSummary result = courtIdentifierGenerator.generate(caseData);

        // then
        assertNotNull(result);
        CourtIdentifier courtIdentifier = result.getCourtIdentifier();
        assertNotNull(courtIdentifier);
        assertEquals(HIGH_COURT, courtIdentifier.getCourtIdentifier());

    }
}
