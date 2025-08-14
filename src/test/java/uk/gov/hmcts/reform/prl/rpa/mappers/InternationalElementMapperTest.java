package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class InternationalElementMapperTest {

    @InjectMocks
    InternationalElementMapper internationalElementMapper;

    @Test
    public void testInternationalElementMapper() {
        CaseData caseData = CaseData.builder().habitualResidentInOtherState(Yes)
            .habitualResidentInOtherStateGiveReason("Habitual Resident In Other Reason").jurisdictionIssueGiveReason(
                "Jurisdiction Reason")
            .requestToForeignAuthority(Yes).requestToForeignAuthorityGiveReason("Request To Foreign Authority Reason")
            .jurisdictionIssue(Yes).build();
        assertNotNull(internationalElementMapper.map(caseData));
    }
}
